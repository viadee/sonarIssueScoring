package de.viadee.sonarIssueScoring.service.desirability.calculation;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoUnit;

import org.apache.commons.math3.analysis.interpolation.LinearInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.sonarqube.ws.Issues.Issue;
import org.springframework.stereotype.Component;

import com.google.common.collect.Multiset;

import de.viadee.sonarIssueScoring.service.desirability.Rating;
import de.viadee.sonarIssueScoring.service.desirability.RatingType;
import de.viadee.sonarIssueScoring.service.desirability.UserPreferences;
import de.viadee.sonarIssueScoring.service.prediction.PredictionResult;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

@Component
public class RatingProviderAge implements RatingProvider {
    private static final DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().append(DateTimeFormatter.ISO_LOCAL_DATE_TIME).//
            optionalStart().appendOffset("+HHMM", "+0000").optionalEnd(). //
            optionalStart().appendOffset("+HH", "Z").optionalEnd().toFormatter();

    private static final PolynomialSplineFunction interpolator = new LinearInterpolator().interpolate(new double[]{0, 7, 30, 365}, new double[]{1.5, 1.4, 1.3, 1});

    @Override
    public Rating createRating(Issue issue, PredictionResult predictionResult, GitPath realPath, UserPreferences userPreferences, Multiset<String> componentCounts) {
        long ageDays = OffsetDateTime.parse(issue.getCreationDate(), dateFormatter).until(OffsetDateTime.now(), ChronoUnit.DAYS);

        return Rating.of(RatingType.Age, interpolator.value(Math.min(365, ageDays)),
                ageDays < 30 ? "Young issues are more pressing to fix" : "Old issues are somewhat less pressing to fix");
    }
}
