package de.viadee.sonarIssueScoring.service.desirability;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

import java.util.Optional;

/**
 * A single rating, part of the Desirability-Score of a specific issue.
 * <p>
 * Optionally contains an explanation why the exact value was chosen.
 */
@SuppressWarnings("ClassReferencesSubclass")
@JsonSerialize
@Immutable
@ImmutableStyle
public abstract class BaseRating {
    public abstract RatingType type();

    public abstract double rating();

    public abstract Optional<String> reason();

    public static Rating of(RatingType type, double rating, String reason) {
        return Rating.of(type, rating, Optional.ofNullable(reason));
    }

    public static Rating of(RatingType type, double rating) {
        return Rating.of(type, rating, Optional.empty());
    }
}
