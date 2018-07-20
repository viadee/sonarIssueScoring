package de.viadee.sonarIssueScoring.service.prediction.train;

import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;

public interface MLService {
    public PredictionResult predict(MLInput input);
}
