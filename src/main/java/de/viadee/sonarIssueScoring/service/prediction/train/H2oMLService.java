package de.viadee.sonarIssueScoring.service.prediction.train;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import de.viadee.sonarIssueScoring.service.prediction.FileInformation;
import de.viadee.sonarIssueScoring.service.prediction.ModelMetrics;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import water.bindings.pojos.FrameKeyV3;
import water.bindings.pojos.LeaderboardV99;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

/**
 * Performs all ML
 */
@Component
public class H2oMLService implements MLService {
    private static final Logger log = LoggerFactory.getLogger(H2oMLService.class);

    private final ObjectMapper objectMapper;
    private final CsvConverter csvConverter;

    public H2oMLService(ObjectMapper objectMapper, CsvConverter csvConverter) {
        this.objectMapper = objectMapper;
        this.csvConverter = csvConverter;
    }

    @Override public PredictionResult predict(MLInput input) {
        checkState(!input.trainingData().isEmpty(), "Can not learn from empty training data");

        log.info("Data loaded, starting learning");
        //TODO figure out a better way to do assisted injection, if possible in spring
        ExtendedH2oApi extendedH2OApi = new ExtendedH2oApi(csvConverter, objectMapper, input.h2oUrl());

        try {
            List<Instance> trainData = input.trainingData().stream().filter(i -> i.fold() >= 0).collect(Collectors.toList());
            List<Instance> testData = input.trainingData().stream().filter(i -> i.fold() == -1).collect(Collectors.toList());
            List<Instance> leaderBoardData = input.trainingData().stream().filter(i -> i.fold() == -2).collect(Collectors.toList());

            FrameKeyV3 train = extendedH2OApi.uploadAndParse(trainData);
            FrameKeyV3 test = extendedH2OApi.uploadAndParse(testData);
            FrameKeyV3 leaderBoardFrame = extendedH2OApi.uploadAndParse(leaderBoardData);
            log.info("Uploaded data (train: {}, test: {})", train, test);

            //First training run to get variable importance.
            LeaderboardV99 resultPreliminary = extendedH2OApi.train(train, test, leaderBoardFrame, 5 * 60, ImmutableList.of());

            ModelMetrics preliminaryMetrics = extendedH2OApi.metrics(resultPreliminary.models[0], test);
            log.info("Found preliminary model ({}) {} ", preliminaryMetrics, resultPreliminary.models[0]);

            //Only interesting result is removable variable instance (Any variable less important than random)
            List<String> lessOrEqualThanRandom = preliminaryMetrics.variableImportances().entrySet().stream().filter(
                    e -> e.getValue() <= preliminaryMetrics.variableImportances().get("random")).map(Entry::getKey).collect(Collectors.toList());

            //Train again, without uninteresting variables
            LeaderboardV99 resultFinal = extendedH2OApi.train(train, test, leaderBoardFrame, 20 * 60, lessOrEqualThanRandom);
            ModelMetrics metricsFinal = extendedH2OApi.metrics(resultFinal.models[0], test);
            log.info("Found final model ({}) {} ", metricsFinal, resultFinal.models[0]);

            FrameKeyV3 predictableInstancesFrame = extendedH2OApi.uploadAndParse(input.predictionData());
            List<Double> predictions = extendedH2OApi.predict(resultFinal.models[0], predictableInstancesFrame);
            extendedH2OApi.delete(predictableInstancesFrame);
            Map<Path, FileInformation> predictionResult = IntStream.range(0, input.predictionData().size()).boxed().collect(
                    toImmutableMap(i -> input.predictionData().get(i).path(), i -> FileInformation.of(predictions.get(i), input.predictionData().get(i).dependants())));


            extendedH2OApi.delete(train, test, leaderBoardFrame);
            extendedH2OApi.delete(resultPreliminary.models);
            extendedH2OApi.delete(resultFinal.models);

            return PredictionResult.of(metricsFinal, predictionResult);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

