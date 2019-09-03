package de.viadee.sonarissuescoring.service.desirability.calculation;

import org.sonarqube.ws.Common.RuleType;
import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multiset;

import de.viadee.sonarissuescoring.service.desirability.Rating;
import de.viadee.sonarissuescoring.service.desirability.RatingType;
import de.viadee.sonarissuescoring.service.desirability.UserPreferences;
import de.viadee.sonarissuescoring.service.prediction.PredictionResult;
import de.viadee.sonarissuescoring.service.prediction.load.GitPath;

@Component
public class RatingProviderType implements RatingProvider {

    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, GitPath realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        if (issue.getType() == RuleType.UNKNOWN || issue.getType() == RuleType.CODE_SMELL)
            return Rating.of(RatingType.RuleType, 1);
        if (issue.getType() == RuleType.VULNERABILITY)
            return Rating.of(RatingType.RuleType, 1.1, "Vulnerabilities should be investigated with a higher priority");
        return Rating.of(RatingType.RuleType, 1.2, "Bugs need to be fixed");
    }
}
