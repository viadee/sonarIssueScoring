package de.viadee.sonarIssueScoring.service.prediction.extract.source;

import java.util.List;

import org.springframework.stereotype.Component;

import de.viadee.sonarIssueScoring.service.prediction.extract.FeatureExtractor;
import de.viadee.sonarIssueScoring.service.prediction.extract.Output;
import de.viadee.sonarIssueScoring.service.prediction.load.Commit;

/**
 * Extracts some features related to CK-Metrics out of the files, by parsing their content.
 */
@Component
public class MetricExtractor implements FeatureExtractor {

    @Override public void extractFeatures(List<Commit> commits, Output out) {
        try (MutableAnalysis analysis = new MutableAnalysis()) {
            for (Commit commit : commits) {
                analysis.update(commit);
                commit.content().forEach((path, content) -> analysis.addAnalysis(path, (k, v) -> out.add(commit, path, k, v)));
            }
        } catch (Exception e) { //Catching all Exceptions because CK likes to throw exceptions like UnsupportedOperationException on some input
            throw new RuntimeException("Could not extract metrics for " + commits, e);
        }
    }
}
