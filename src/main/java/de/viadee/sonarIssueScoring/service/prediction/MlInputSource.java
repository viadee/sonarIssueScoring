package de.viadee.sonarIssueScoring.service.prediction;

import static com.google.common.base.Preconditions.*;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import de.viadee.sonarIssueScoring.service.prediction.extract.FeatureExtractor;
import de.viadee.sonarIssueScoring.service.prediction.extract.Output;
import de.viadee.sonarIssueScoring.service.prediction.extract.TargetExtractor;
import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance;
import de.viadee.sonarIssueScoring.service.prediction.train.MLInput;

/**
 * Extracts a list of instances out of extracted Repo / Commit data
 */
@Component
public class MlInputSource {
    private static final Logger log = LoggerFactory.getLogger(MlInputSource.class);
    private final List<FeatureExtractor> featureExtractors;
    private final TargetExtractor targetExtractor;

    public MlInputSource(List<FeatureExtractor> featureExtractors, TargetExtractor targetExtractor) {
        this.featureExtractors = featureExtractors;
        this.targetExtractor = targetExtractor;
    }

    public enum Mode {
        ActualFuture,
        Evaluate
    }

    /**
     * Creates instances to train and predict on.
     * <p>
     * horizon denotes the future in which the target variable is extracted.
     * <p>
     * Example commit usages (T = Training instances, P = Prediction instances, - = not used)
     * <p>
     * Mode = Actual, and horizon = 3
     * Commit-Index: 0 1 2 3 4 5 6 7 8
     * Commit-Use:   T T T T T T - - P
     * <p>
     * Mode = Evaluate, and horizon = 3
     * Commit-Index: 0 1 2 3 4 5 6 7 8
     * Commit-Use:   T T T - - P - - -
     */
    public MLInput createMLInput(List<Commit> commits, String h2oServer, int horizon, Mode mode) {
        checkState(commits.size() > horizon + 1, "Not enough commits to predict, requiring %s, got %s", horizon, commits.size());

        Multimap<Commit, Instance> instances = createInstances(commits, horizon);

        int predictedCommit = mode == Mode.ActualFuture ? commits.size() - 1 : commits.size() - horizon - 1;

        List<Commit> trainingBase = commits.subList(0, predictedCommit - horizon);
        Commit predicted = commits.get(predictedCommit);

        ImmutableList<Instance> trainingInstances = trainingBase.stream().flatMap(c -> instances.get(c).stream()).collect(ImmutableList.toImmutableList());
        return MLInput.of(trainingInstances, instances.get(predicted), h2oServer);
    }

    private Multimap<Commit, Instance> createInstances(List<Commit> commits, int horizon) {
        Output out = new Output(commits);
        featureExtractors.forEach(featureExtractor -> {
            log.info("Starting extraction: {}", featureExtractor.getClass().getSimpleName());
            featureExtractor.extractFeatures(commits, out);
        });

        log.info("Starting extraction of target variable");
        for (int i = 0; i < commits.size() - horizon; i++) {
            targetExtractor.extractTargetVariable(commits.get(i), commits.subList(i + 1, i + horizon + 1), out);
            targetExtractor.extractTrainingHelpers(commits.get(i), out);
        }

        return out.build();
    }
}
