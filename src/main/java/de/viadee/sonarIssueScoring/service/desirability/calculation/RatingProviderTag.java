package de.viadee.sonarIssueScoring.service.desirability.calculation;

import com.google.common.collect.Multiset;
import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.RatingType;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.function.Predicate;

public class RatingProviderTag implements RatingProvider {
    private final Rating ratingFound;
    private final Rating ratingNotFound;
    private final Predicate<String> tagApplies;

    public RatingProviderTag(Predicate<String> tagApplies, double ratingIfFound, RatingType ratingType) {
        this.tagApplies = tagApplies;
        ratingFound = Rating.of(ratingType, ratingIfFound, "This issue has tags corresponding to " + ratingType);
        ratingNotFound = Rating.of(ratingType, 1, "This issue has no tag corresponding to " + ratingType);
    }

    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, Path realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        return issue.getTagsList().stream().filter(tagApplies).findAny().map(present -> ratingFound).orElse(ratingNotFound);
    }

    @Component
    public static class RatingProviderSecurity extends RatingProviderTag {
        public RatingProviderSecurity() {
            super(tag -> tag.equals("cwe") || tag.equals("security") || tag.startsWith("owasp-") || tag.startsWith("sans-"), 1.4, RatingType.TagsSecurity);
        }
    }

    @Component
    public static class RatingProviderBugs extends RatingProviderTag {
        public RatingProviderBugs() {
            super(tag -> tag.equals("bug") || tag.equals("suspicious"), 1.4, RatingType.TagsBug);
        }
    }

    @Component
    public static class RatingProviderTagTooHardToUnderstand extends RatingProviderTag {
        public RatingProviderTagTooHardToUnderstand() {
            super(tag -> tag.equals("brain-overload") || tag.equals("clumsy") || tag.equals("confusing") || tag.equals("convention"), 1.3,
                    RatingType.TagsHardToUnderstand);
        }
    }

    @Component
    public static class RatingProviderPossibleFutureProblems extends RatingProviderTag {
        public RatingProviderPossibleFutureProblems() {
            super(tag -> tag.equals("pitfall") || tag.equals("unpredictable"), 1.1, RatingType.TagsPossibleFutureProblems);
        }
    }
}
