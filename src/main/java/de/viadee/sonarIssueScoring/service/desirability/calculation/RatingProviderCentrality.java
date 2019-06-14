package de.viadee.sonarIssueScoring.service.desirability.calculation;

import com.google.common.collect.Multiset;
import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.RatingType;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

@Component
public class RatingProviderCentrality implements RatingProvider {
    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, GitPath realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        if (predictionResult.results().containsKey(realPath))
            return Rating.of(RatingType.Centrality, 1 + predictionResult.results().get(realPath).dependentCount() * 0.05);
        return Rating.of(RatingType.Centrality, 1);
    }
}
