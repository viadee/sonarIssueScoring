package de.viadee.sonarissuescoring.service.desirability;

import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

/**
 * The desirability of an issue is the desire / need of a software development team for it to be fixed.
 * <p>
 * This class contains the final Desirability-Score for a specific issue, as well as all its individual ratings to explain it
 */
@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
@JsonSerialize
public final class IssueDesirability {
    private final ImmutableList<Rating> ratings;
    private final double desirabilityScore;

    private IssueDesirability(Iterable<Rating> ratings, double desirabilityScore) {
        this.ratings = ImmutableList.copyOf(ratings);
        this.desirabilityScore = desirabilityScore;
    }

    public static IssueDesirability of(Iterable<Rating> ratings, double desirabilityScore) {
        return new IssueDesirability(ratings, desirabilityScore);
    }

    @JsonProperty public ImmutableList<Rating> ratings() {
        return ratings;
    }

    @JsonProperty public double desirabilityScore() {
        return desirabilityScore;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        IssueDesirability that = (IssueDesirability) o;
        return Double.compare(that.desirabilityScore, desirabilityScore) == 0 && ratings.equals(that.ratings);
    }

    @Override public int hashCode() {
        return Objects.hash(ratings, desirabilityScore);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("IssueDesirability").omitNullValues().
                add("ratings", ratings).
                add("desirabilityScore", desirabilityScore).
                toString();
    }
}
