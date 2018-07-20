package de.viadee.sonarIssueScoring.service.desirability.calculation;

import com.google.common.collect.Multiset;
import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.RatingType;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.*;

@Component
public class RatingProviderEffort implements RatingProvider {
    private static final Pattern patternEffort = Pattern.compile("((\\d+)h)?((\\d+)min)?");
    private static final PolynomialSplineFunction interpolator = new LinearInterpolator().interpolate(new double[]{0, 60, 120, 240}, new double[]{1.4, 1, 0.9, 0.8});

    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, Path realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        Matcher m = patternEffort.matcher(issue.getEffort());
        checkState(m.matches(), "Issue %s has non-conforming effort value: %s", issue, issue.getEffort());

        int effortMinutes = parseIfPresent(m.group(2)) * 60 + parseIfPresent(m.group(4));

        return Rating.of(RatingType.Effort, interpolator.value(Math.min(240, effortMinutes)),
                effortMinutes > 60 ? "This sonar is very extensive to fix, other issues might be more pressing to fix" : null);
    }

    private static int parseIfPresent(String group) { return group == null ? 0 : Integer.parseInt(group);}
}
