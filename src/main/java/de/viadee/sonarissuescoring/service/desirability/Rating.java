package de.viadee.sonarissuescoring.service.desirability;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;

/**
 * A single rating, part of the Desirability-Score of a specific issue.
 * <p>
 * Optionally contains an explanation why the exact value was chosen.
 */
@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
@JsonSerialize
public final class Rating {
    private final RatingType type;
    private final double rating;
    private final Optional<String> reason;

    private Rating(RatingType type, double rating, Optional<String> reason) {
        this.type = Objects.requireNonNull(type, "type");
        this.rating = rating;
        this.reason = Objects.requireNonNull(reason, "reason");
    }

    public static Rating of(RatingType type, double rating, Optional<String> reason) { return new Rating(type, rating, reason);}

    public static Rating of(RatingType type, double rating, @Nullable String reason) {
        return of(type, rating, Optional.ofNullable(reason));
    }

    public static Rating of(RatingType type, double rating) {
        return of(type, rating, Optional.empty());
    }

    @JsonProperty public RatingType type() {
        return type;
    }

    @JsonProperty public double rating() {
        return rating;
    }

    public Rating withRating(double newRating) {
        return new Rating(type, newRating, reason);
    }

    @JsonProperty public Optional<String> reason() {
        return reason;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Rating rating1 = (Rating) o;
        return Double.compare(rating1.rating, rating) == 0 && type == rating1.type && reason.equals(rating1.reason);
    }

    @Override public int hashCode() {
        return Objects.hash(type, rating, reason);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("Rating").omitNullValues().add("type", type).add("rating", rating).add("reason", reason).toString();
    }
}
