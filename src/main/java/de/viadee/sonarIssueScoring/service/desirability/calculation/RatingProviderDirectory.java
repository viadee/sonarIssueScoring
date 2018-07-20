package de.viadee.sonarIssueScoring.service.desirability.calculation;

import com.google.common.collect.Multiset;
import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.RatingType;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class RatingProviderDirectory implements RatingProvider {
    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, Path realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        return getDirectoryRating(realPath.getParent(), userPreferences);
    }

    @SuppressWarnings("TailRecursion") private static Rating getDirectoryRating(Path path, UserPreferences userPreferences) {
        if (userPreferences.directoryScores().containsKey(path))
            return Rating.of(RatingType.Directory, userPreferences.directoryScores().get(path), "Manually assigned score");
        if (path.getParent() == null)
            return Rating.of(RatingType.Directory, 1);
        return getDirectoryRating(path.getParent(), userPreferences);
    }
}
