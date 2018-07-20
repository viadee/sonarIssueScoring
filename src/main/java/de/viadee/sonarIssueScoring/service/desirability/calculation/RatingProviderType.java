package de.viadee.sonarIssueScoring.service.desirability.calculation;

import com.google.common.collect.Multiset;
import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.RatingType;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import org.sonarqube.ws.Common.RuleType;
import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class RatingProviderType implements RatingProvider {

    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, Path realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        if (issue.getType() == RuleType.UNKNOWN || issue.getType() == RuleType.CODE_SMELL)
            return Rating.of(RatingType.RuleType, 1);
        if (issue.getType() == RuleType.VULNERABILITY)
            return Rating.of(RatingType.RuleType, 1.1, "Vulnerabilities should be investigated with a higher priority");
        return Rating.of(RatingType.RuleType, 1.2, "Bugs need to be fixed");
    }
}
