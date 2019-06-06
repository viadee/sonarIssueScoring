package de.viadee.sonarIssueScoring.service.prediction.load;

import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

/**
 * Main class for all training data, contains a number data pairs for model training, and the repository state to use for prediction with the final model
 */
@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public final class SplitRepository {
    private final Repo completePast;
    private final ImmutableList<PastFuturePair> trainingData;

    private SplitRepository(Repo completePast, Iterable<PastFuturePair> trainingData) {
        this.completePast = Objects.requireNonNull(completePast, "completePast");
        this.trainingData = ImmutableList.copyOf(trainingData);
    }

    public static SplitRepository of(Repo completePast, Iterable<PastFuturePair> trainingData) { return new SplitRepository(completePast,  trainingData);}

    /**
     * complete repository past for final prediction
     */
    public Repo completePast() {
        return completePast;
    }

    /**
     * base data for instance extraction
     */
    public ImmutableList<PastFuturePair> trainingData() {
        return trainingData;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SplitRepository that = (SplitRepository) o;
        return completePast.equals(that.completePast) && trainingData.equals(that.trainingData);
    }

    @Override public int hashCode() {
        return Objects.hash(completePast, trainingData);
    }
    @Override public String toString() {
        return MoreObjects.toStringHelper("SplitRepository").omitNullValues().add("completePast", completePast).add("trainingData", trainingData).toString();
    }
}
