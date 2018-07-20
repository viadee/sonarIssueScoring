package de.viadee.sonarIssueScoring.service.prediction.extract;

import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;

import java.nio.file.Path;
import java.util.Map;

public interface FeatureExtractor {
    /**
     * Extracts a number rof features out of the repo, putting the into the builders in the output map
     */
    public void extractFeatures(Repo repo, Map<Path, Builder> output);
}
