package de.viadee.sonarIssueScoring.service.prediction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableTable;
import com.google.common.math.PairedStatsAccumulator;
import com.google.common.math.Quantiles;
import com.google.common.math.Quantiles.ScaleAndIndex;

import de.viadee.sonarIssueScoring.service.PredictionParams;
import de.viadee.sonarIssueScoring.service.prediction.MlInputSource.Mode;
import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.load.RepositoryLoader;
import de.viadee.sonarIssueScoring.service.prediction.train.CsvConverter;
import de.viadee.sonarIssueScoring.service.prediction.train.MLInput;
import de.viadee.sonarIssueScoring.service.prediction.train.MLService;

@Component
public class PredictionService {

    private final RepositoryLoader repositoryLoader;
    private final MlInputSource mlInputSource;
    private final MLService mlService;
    private final CsvConverter csvConverter;

    public PredictionService(RepositoryLoader repositoryLoader, MlInputSource mlInputSource, MLService mlService, CsvConverter csvConverter) {
        this.repositoryLoader = repositoryLoader;
        this.mlInputSource = mlInputSource;
        this.mlService = mlService;
        this.csvConverter = csvConverter;
    }

    public PredictionResult predict(PredictionParams params, String h2oServer) {
        List<Commit> commits = repositoryLoader.loadSplitRepository(params);

        return mlService.predict(mlInputSource.createMLInput(commits, h2oServer, params.predictionHorizon(), Mode.ActualFuture));
    }

    /** Extract data and build a model for the past, and compare it with the more recent, not learned past to gauge prediction quality */
    public EvaluationResult evaluate(PredictionParams params, String h2oServer, boolean dumpData) throws IOException {
        List<Commit> commits = repositoryLoader.loadSplitRepository(params);

        MLInput mlInput = mlInputSource.createMLInput(commits, h2oServer, params.predictionHorizon(), Mode.Evaluate);

        if (dumpData) {
            long time = System.currentTimeMillis();
            Files.write(Paths.get("data-" + time + "-train.csv"), csvConverter.toCSV(mlInput.trainingData()).data());
            Files.write(Paths.get("data-" + time + "-predict.csv"), csvConverter.toCSV(mlInput.predictionData()).data());
        }


        PredictionResult result = mlService.predict(mlInput);

        // Collect predicted vs actual future
        List<EvaluationResultPath> pairs = mlInput.predictionData().stream().map(
                instance -> EvaluationResultPath.of(instance.path(), result.results().get(instance.path()).predictedChangeCount(), instance.target())).
                collect(Collectors.toList());

        return EvaluationResult.of(result.validationMetrics(), rmse(pairs), r2(pairs), confusionMatrix(pairs), pairs);
    }

    static ImmutableTable<Boolean, Boolean, Integer> confusionMatrix(Collection<EvaluationResultPath> pairs) {
        //Identify commonly-edited files: all files edited more than the percentile below
        ScaleAndIndex requirement = Quantiles.percentiles().index(80);
        double thresholdActual = requirement.computeInPlace(pairs.stream().mapToDouble(EvaluationResultPath::actual).toArray());
        double thresholdPredicted = requirement.computeInPlace(requirement.computeInPlace(pairs.stream().mapToDouble(EvaluationResultPath::predicted).toArray()));

        return pairs.stream().collect(ImmutableTable.toImmutableTable(//
                pair -> pair.actual() >= thresholdActual, // Row = actual
                pair -> pair.predicted() >= thresholdPredicted, // Col == predicted
                pair -> 1, Integer::sum));
    }

    static double rmse(Collection<EvaluationResultPath> pair) {
        return Math.sqrt(pair.stream().mapToDouble(p -> Math.pow(p.actual() - p.predicted(), 2)).average().orElse(0));
    }

    static double r2(Collection<EvaluationResultPath> pair) {
        PairedStatsAccumulator acc = new PairedStatsAccumulator();
        pair.forEach(p -> acc.add(p.actual(), p.predicted()));
        return Math.pow(acc.pearsonsCorrelationCoefficient(), 2);
    }
}

