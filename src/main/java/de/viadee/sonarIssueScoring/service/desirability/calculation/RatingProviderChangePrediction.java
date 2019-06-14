package de.viadee.sonarIssueScoring.service.desirability.calculation;

import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multiset;

import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.RatingType;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

@Component
public class RatingProviderChangePrediction implements RatingProvider {
    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, GitPath realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        if (predictionResult.results().containsKey(realPath))
            return Rating.of(RatingType.ChangePrediction, 1 + predictionResult.results().get(realPath).predictedChangeCount() * 2);
        return Rating.of(RatingType.ChangePrediction, 1);
    }
}
