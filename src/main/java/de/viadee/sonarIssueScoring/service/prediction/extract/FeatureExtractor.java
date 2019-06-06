package de.viadee.sonarIssueScoring.service.prediction.extract;

import de.viadee.sonarIssueScoring.service.prediction.load.Repo;

public interface FeatureExtractor {
    /**
     * Extracts a number of features out of the repo
     */
    public void extractFeatures(Repo repo, Output out);
}
