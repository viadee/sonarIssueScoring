package de.viadee.sonarIssueScoring.service.desirability;

import java.util.Optional;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;

/**
 * A single rating, part of the Desirability-Score of a specific issue.
 * <p>
 * Optionally contains an explanation why the exact value was chosen.
 */
@SuppressWarnings({"ClassReferencesSubclass", "DefaultAnnotationParam"})
@JsonSerialize
@Immutable(copy = true) //Explicitly needed, as the changed defaults in @ImmutableStyle disable the with* Mutators
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
