package de.viadee.sonarIssueScoring.service.desirability.calculation;

import com.google.common.collect.Multiset;
import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import org.sonarqube.ws.Issues.Issue;

import java.nio.file.Path;

/**
 * Provides a rating for the supplied issue
 * <p>
 * Each provider is focused on a different RatingType
 */
public interface RatingProvider {
    public Rating createRating(Issue issue, PredictionResult predictionResult, Path realPath, UserPreferences userPreferences, Multiset<String> componentCounts);
}
