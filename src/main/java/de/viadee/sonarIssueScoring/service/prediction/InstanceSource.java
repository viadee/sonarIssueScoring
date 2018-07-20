package de.viadee.sonarIssueScoring.service.prediction;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import de.viadee.sonarIssueScoring.service.prediction.extract.FeatureExtractor;
import de.viadee.sonarIssueScoring.service.prediction.extract.TargetExtractor;
import de.viadee.sonarIssueScoring.service.prediction.load.PastFuturePair;
import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

/**
 * Extracts a list of instances out of extracted Repo / Commit data
 */
@Component
public class InstanceSource {
    private final List<FeatureExtractor> featureExtractors;
    private final TargetExtractor targetExtractor;

    public InstanceSource(List<FeatureExtractor> featureExtractors, TargetExtractor targetExtractor) {
        this.featureExtractors = featureExtractors;
        this.targetExtractor = targetExtractor;
    }

    private ImmutableMap<Path, Builder> createBuilders(Repo past) {
        ImmutableMap<Path, Builder> output = past.snapshot().getAllFiles().keySet().stream().collect(
                ImmutableMap.toImmutableMap(path -> path, path -> Instance.builder().path(path)));

        featureExtractors.forEach(extractor -> extractor.extractFeatures(past, output));

        return output;
    }

    private static List<Instance> finalize(ImmutableMap<Path, Builder> builders) {
        return builders.values().stream().map(Builder::build).collect(ImmutableList.toImmutableList());
    }

    /** Extracts instances for a repository representing the past, with an unknown future (target and fold value are meaningless) */
    public List<Instance> extractInstances(Repo past) {
        ImmutableMap<Path, Builder> builders = createBuilders(past);
        targetExtractor.fillDummyTargetValues(builders);
        return finalize(builders);
    }

    public List<Instance> extractInstances(List<PastFuturePair> pastFuturePair) {
        return pastFuturePair.stream().flatMap(pair -> {
            ImmutableMap<Path, Builder> output = createBuilders(pair.past());
            targetExtractor.extractTargetVariable(pair.future(), output);
            return finalize(output).stream();
        }).collect(ImmutableList.toImmutableList());
    }
}
