package de.viadee.sonarIssueScoring.service.prediction;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableTable;
import com.google.common.math.PairedStatsAccumulator;
import com.google.common.math.Quantiles;
import com.google.common.math.Quantiles.ScaleAndIndex;

import de.viadee.sonarIssueScoring.service.PredictionParams;
import de.viadee.sonarIssueScoring.service.prediction.load.RepositoryLoader;
import de.viadee.sonarIssueScoring.service.prediction.load.SnapshotStrategy;
import de.viadee.sonarIssueScoring.service.prediction.load.SplitRepository;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance;
import de.viadee.sonarIssueScoring.service.prediction.train.MLInput;
import de.viadee.sonarIssueScoring.service.prediction.train.MLService;

@Component
public class PredictionService {

    private final RepositoryLoader repositoryLoader;
    private final InstanceSource instanceSource;
    private final MLService mlService;

    public PredictionService(RepositoryLoader repositoryLoader, InstanceSource instanceSource, MLService mlService) {
        this.repositoryLoader = repositoryLoader;
        this.instanceSource = instanceSource;
        this.mlService = mlService;
    }

    public PredictionResult predict(PredictionParams params, String h2oServer) {
        SplitRepository data = repositoryLoader.loadSplitRepository(params, SnapshotStrategy.OVERLAP_ALWAYS);

        List<Instance> instances = instanceSource.extractInstances(data.trainingData());
        List<Instance> predictableInstances = instanceSource.extractInstances(data.completePast());

        return mlService.predict(MLInput.of(instances, predictableInstances, h2oServer));
    }

    /** Extract data and build a model for the past, and compare it with the more recent, not learned past to gauge prediction quality */
    public EvaluationResult evaluate(PredictionParams params, String h2oServer) {
        SplitRepository data = repositoryLoader.loadSplitRepository(params, SnapshotStrategy.NO_OVERLAP_ON_MOST_RECENT);
        //Use the most recent pastFuturePair as actual future, which has to be predicted. The SnapshotStrategy assures this future is not used as training data, even partially

        Preconditions.checkState(data.trainingData().size() > 1, "Not enough historical data");

        List<Instance> instances = instanceSource.extractInstances(data.trainingData().subList(1, data.trainingData().size())); //Training data, based on past
        List<Instance> predictableInstances = instanceSource.extractInstances(data.trainingData().subList(0, 1));

        PredictionResult result = mlService.predict(MLInput.of(instances, predictableInstances, h2oServer));

        // Collect predicted vs actual future
        List<ResultPair> pairs = predictableInstances.stream().map(
                instance -> new ResultPair(result.results().get(instance.path()).predictedChangeCount(), instance.targetEditCountPercentile())).collect(
                Collectors.toList());

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
                pair -> 1, (a, b) -> a + b));
    }

    static double rmse(Collection<ResultPair> pair) {
        return Math.sqrt(pair.stream().mapToDouble(p -> Math.pow(p.actual - p.predicted, 2)).average().orElse(0));
    }

    static double r2(Collection<ResultPair> pair) {
        PairedStatsAccumulator acc = new PairedStatsAccumulator();
        pair.forEach(p -> acc.add(p.actual, p.predicted));
        return Math.pow(acc.pearsonsCorrelationCoefficient(), 2);
    }

    static class ResultPair {
        private final double predicted, actual;

        ResultPair(double predicted, double actual) {
            this.predicted = predicted;
            this.actual = actual;
        }
    }
}

