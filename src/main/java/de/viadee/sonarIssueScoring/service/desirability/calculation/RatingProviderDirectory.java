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
public class RatingProviderDirectory implements RatingProvider {
    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, GitPath realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        return getDirectoryRating(realPath.dir(), userPreferences);
    }

    @SuppressWarnings("TailRecursion") private static Rating getDirectoryRating(GitPath path, UserPreferences userPreferences) {
        if (userPreferences.directoryScores().containsKey(path))
            return Rating.of(RatingType.Directory, userPreferences.directoryScores().get(path), "Manually assigned score");
        if (path.dir() == null)
            return Rating.of(RatingType.Directory, 1);
        return getDirectoryRating(path.dir(), userPreferences);
    }
}
