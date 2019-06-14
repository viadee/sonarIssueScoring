package de.viadee.sonarIssueScoring.service.desirability.calculation;

import com.google.common.collect.Multiset;
import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.RatingType;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

import org.sonarqube.ws.Common.Severity;
import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

@Component
public class RatingProviderSeverity implements RatingProvider {

    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, GitPath realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        if (issue.getSeverity() == Severity.BLOCKER)
            return Rating.of(RatingType.Severity, 1.8, "Blocking issues are blocking everything else, and should be handled with priority");
        if (issue.getSeverity() == Severity.CRITICAL)
            return Rating.of(RatingType.Severity, 1.5, "Critical issues are somewhat important");
        if (issue.getSeverity() == Severity.MAJOR)
            return Rating.of(RatingType.Severity, 1.3);
        if (issue.getSeverity() == Severity.MINOR)
            return Rating.of(RatingType.Severity, 1.15);
        return Rating.of(RatingType.Severity, 1);
    }
}
