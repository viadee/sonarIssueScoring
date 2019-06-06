package de.viadee.sonarIssueScoring.service.prediction.load;

import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;

/**
 * Data for instance create for the training: Contains a pair of past data and the corresponding predictable future data
 */
@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public final class PastFuturePair {
    private final Repo past;
    private final Repo future;

    private PastFuturePair(Repo past, Repo future) {
        this.past = Objects.requireNonNull(past, "past");
        this.future = Objects.requireNonNull(future, "future");
    }

    public static PastFuturePair of(Repo past, Repo future) {
        return new PastFuturePair(past, future);
    }

    public Repo past() {
        return past;
    }

    public Repo future() {
        return future;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PastFuturePair that = (PastFuturePair) o;
        return past.equals(that.past) && future.equals(that.future);
    }

    @Override public int hashCode() {
        return Objects.hash(past, future);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("PastFuturePair").omitNullValues().add("past", past).add("future", future).toString();
    }
}
