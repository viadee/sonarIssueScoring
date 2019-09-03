package de.viadee.sonarissuescoring.service.prediction.extract;

import java.util.List;

import de.viadee.sonarissuescoring.service.prediction.load.Commit;

public interface FeatureExtractor {
    /**
     * Extracts a number of features out of the repo
     *
     * @param commits All commits to the repository, starting with the inital commit, ending at HEAD
     */
    public void extractFeatures(List<Commit> commits, Output out);
}
