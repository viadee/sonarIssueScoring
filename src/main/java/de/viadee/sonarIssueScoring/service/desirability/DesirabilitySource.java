package de.viadee.sonarIssueScoring.service.desirability;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Maps;

import de.viadee.sonarIssueScoring.service.PredictionParams;
import de.viadee.sonarIssueScoring.service.desirability.calculation.RatingProvider;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import de.viadee.sonarIssueScoring.service.prediction.PredictionService;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;
import de.viadee.sonarIssueScoring.service.sonar.SonarIssueSource;

/**
 * Main orchestrator for the calculation of the Desirability-Score
 */
@Service
public class DesirabilitySource {
    private final SonarIssueSource sonarIssueSource;
    private final List<RatingProvider> ratingProviders;
    private final PredictionService predictionService;

    public DesirabilitySource(SonarIssueSource sonarIssueSource, List<RatingProvider> ratingProviders, PredictionService predictionService) {
        this.sonarIssueSource = sonarIssueSource;
        this.ratingProviders = ratingProviders;
        this.predictionService = predictionService;
    }

    public Map<String, IssueDesirability> calculateIssueDesirability(UserPreferences userPreferences) {
        List<Issue> issues = sonarIssueSource.findAll(userPreferences.sonarServer(), userPreferences.sonarProjectId());

        PredictionResult prediction = predictionService.predict(PredictionParams.of(userPreferences.gitServer(), userPreferences.predictionHorizon()),
                userPreferences.h2oUrl());

        //Required for merging component ids back to filenames
        PathSuffixLookup<GitPath> realPathLookup = new PathSuffixLookup<>(Maps.uniqueIndex(prediction.results().keySet(), p -> p));

        ImmutableMultiset<String> componentCounts = issues.stream().map(Issue::getComponent).collect(ImmutableMultiset.toImmutableMultiset());

        return issues.stream().collect(
                Collectors.toMap(Issue::getKey, issue -> createDesirabilityInformation(issue, realPathLookup, prediction, userPreferences, componentCounts)));
    }

    private IssueDesirability createDesirabilityInformation(Issue issue, PathSuffixLookup<GitPath> realPathLookup, PredictionResult prediction,
                                                            UserPreferences userPreferences, ImmutableMultiset<String> componentCounts) {

        //There is no way to get the path in the repository from information from the sonarQube api.
        GitPath path = GitPath.of(issue.getComponent().substring(issue.getComponent().lastIndexOf(':') + 1));

        List<Rating> ratings = ratingProviders.stream().
                map(r -> r.createRating(issue, prediction, realPathLookup.getOrDefault(path, path), userPreferences, componentCounts)). //Gather all ratings
                map(r -> r.withRating(1 + (r.rating() - 1) * userPreferences.ratingWeights().getOrDefault(r.type(), 1.0))). //Rate by user-supplied weights
                collect(Collectors.toList());

        return IssueDesirability.of(ratings, ratings.stream().mapToDouble(Rating::rating).reduce(1, (a, b) -> a * b));
    }
}
