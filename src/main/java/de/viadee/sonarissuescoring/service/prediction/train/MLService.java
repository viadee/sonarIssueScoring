package de.viadee.sonarissuescoring.service.prediction.train;

import de.viadee.sonarissuescoring.service.prediction.PredictionResult;

public interface MLService {
    public PredictionResult predict(MLInput input);
}
