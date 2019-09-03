package de.viadee.sonarissuescoring.service.desirability.calculation;

import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multiset;

import de.viadee.sonarissuescoring.service.desirability.Rating;
import de.viadee.sonarissuescoring.service.desirability.RatingType;
import de.viadee.sonarissuescoring.service.desirability.UserPreferences;
import de.viadee.sonarissuescoring.service.prediction.PredictionResult;
import de.viadee.sonarissuescoring.service.prediction.load.GitPath;

@Component
public class RatingProviderIssuesInFile implements RatingProvider {

    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, GitPath realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        return Rating.of(RatingType.NumberOfIssuesInFile, 0.95 + componentCounts.count(issue.getComponent()) / 20.0);
    }
}
