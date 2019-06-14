package de.viadee.sonarIssueScoring.service.desirability.calculation;

import org.sonarqube.ws.Issues.Issue;

import com.google.common.collect.Multiset;

import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

/**
 * Provides a rating for the supplied issue
 * <p>
 * Each provider is focused on a different RatingType
 */
public interface RatingProvider {
    public Rating createRating(Issue issue, PredictionResult predictionResult, GitPath realPath, UserPreferences userPreferences, Multiset<String> componentCounts);
}
