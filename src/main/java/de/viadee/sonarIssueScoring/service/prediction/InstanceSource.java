package de.viadee.sonarIssueScoring.service.prediction;

import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

import de.viadee.sonarIssueScoring.service.prediction.extract.FeatureExtractor;
import de.viadee.sonarIssueScoring.service.prediction.extract.Output;
import de.viadee.sonarIssueScoring.service.prediction.extract.TargetExtractor;
import de.viadee.sonarIssueScoring.service.prediction.load.PastFuturePair;
import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance;

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

    private List<Instance> create(Repo past) {
        Output out = new Output(past.currentContent().keySet());
        featureExtractors.forEach(extractor -> extractor.extractFeatures(past, out));
        return out.build();
    }

    /** Extracts instances for a repository representing the past, with an unknown future (target and fold value are meaningless) */
    public List<Instance> extractInstances(Repo past) {
        return create(past);
    }

    public List<Instance> extractInstances(List<PastFuturePair> pairs) {
        return pairs.stream().flatMap(pair -> {
            Output out = new Output(pair.past().currentContent().keySet());
            featureExtractors.forEach(extractor -> extractor.extractFeatures(pair.past(), out));
            targetExtractor.extractTargetVariable(pair.future(), out);
            return out.build().stream();
        }).collect(ImmutableList.toImmutableList());
    }
}
