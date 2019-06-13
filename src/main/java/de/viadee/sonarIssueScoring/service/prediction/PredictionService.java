package de.viadee.sonarIssueScoring.service.prediction;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableTable;
import com.google.common.math.PairedStatsAccumulator;
import com.google.common.math.Quantiles;
import com.google.common.math.Quantiles.ScaleAndIndex;

import de.viadee.sonarIssueScoring.service.PredictionParams;
import de.viadee.sonarIssueScoring.service.prediction.MlInputSource.Mode;
import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.load.RepositoryLoader;
import de.viadee.sonarIssueScoring.service.prediction.train.MLInput;
import de.viadee.sonarIssueScoring.service.prediction.train.MLService;

@Component
public class PredictionService {

    private final RepositoryLoader repositoryLoader;
    private final MlInputSource mlInputSource;
    private final MLService mlService;

    public PredictionService(RepositoryLoader repositoryLoader, MlInputSource mlInputSource, MLService mlService) {
        this.repositoryLoader = repositoryLoader;
        this.mlInputSource = mlInputSource;
        this.mlService = mlService;
    }

    public PredictionResult predict(PredictionParams params, String h2oServer) {
        List<Commit> commits = repositoryLoader.loadSplitRepository(params);

        return mlService.predict(mlInputSource.createMLInput(commits, h2oServer, params.predictionHorizon(), Mode.ActualFuture));
    }

    /** Extract data and build a model for the past, and compare it with the more recent, not learned past to gauge prediction quality */
    public EvaluationResult evaluate(PredictionParams params, String h2oServer) {
        List<Commit> commits = repositoryLoader.loadSplitRepository(params);

        MLInput mlInput = mlInputSource.createMLInput(commits, h2oServer, params.predictionHorizon(), Mode.Evaluate);
        PredictionResult result = mlService.predict(mlInput);

        // Collect predicted vs actual future
        List<ResultPair> pairs = mlInput.predictionData().stream().map(
                instance -> new ResultPair(result.results().get(instance.path()).predictedChangeCount(), instance.target())).collect(Collectors.toList());

        return EvaluationResult.of(rmse(pairs), r2(pairs), confusionMatrix(pairs));
    }

    static ImmutableTable<Boolean, Boolean, Integer> confusionMatrix(Collection<ResultPair> pairs) {
        //Identify commonly-edited files: all files edited more than the percentile below
        ScaleAndIndex requirement = Quantiles.percentiles().index(80);
        double thresholdActual = requirement.computeInPlace(pairs.stream().mapToDouble(p -> p.actual).toArray());
        double thresholdPredicted = requirement.computeInPlace(requirement.computeInPlace(pairs.stream().mapToDouble(p -> p.predicted).toArray()));

        return pairs.stream().collect(ImmutableTable.toImmutableTable(//
                pair -> pair.actual >= thresholdActual, // Row = actual
                pair -> pair.predicted >= thresholdPredicted, // Col == predicted
                pair -> 1, Integer::sum));
    }

    static double rmse(Collection<ResultPair> pair) {
        return Math.sqrt(pair.stream().mapToDouble(p -> Math.pow(p.actual - p.predicted, 2)).average().orElse(0));
    }

    static double r2(Collection<ResultPair> pair) {
        PairedStatsAccumulator acc = new PairedStatsAccumulator();
        pair.forEach(p -> acc.add(p.actual, p.predicted));
        return Math.pow(acc.pearsonsCorrelationCoefficient(), 2);
    }

    @VisibleForTesting
    static class ResultPair {
        private final double predicted;
        private final double actual;

        ResultPair(double predicted, double actual) {
            this.predicted = predicted;
            this.actual = actual;
        }
    }
}

