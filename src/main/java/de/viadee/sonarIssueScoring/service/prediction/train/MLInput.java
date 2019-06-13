package de.viadee.sonarIssueScoring.service.prediction.train;

import java.util.List;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public final class MLInput {
    private final ImmutableList<Instance> trainingData;
    private final ImmutableList<Instance> predictionData;
    private final String h2oUrl;

    private MLInput(ImmutableList<Instance> trainingData, ImmutableList<Instance> predictionData, String h2oUrl) {
        this.trainingData = trainingData;
        this.predictionData = predictionData;
        this.h2oUrl = Objects.requireNonNull(h2oUrl, "h2oUrl");
    }

    public static MLInput of(List<Instance> trainingData, Iterable<Instance> predictionData, String h2oUrl) {
        return new MLInput(ImmutableList.copyOf(trainingData), ImmutableList.copyOf(predictionData), h2oUrl);
    }

    public List<Instance> trainingData() {
        return trainingData;
    }

    public List<Instance> predictionData() {
        return predictionData;
    }

    public String h2oUrl() {
        return h2oUrl;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MLInput mlInput = (MLInput) o;
        return trainingData.equals(mlInput.trainingData) && predictionData.equals(mlInput.predictionData) && h2oUrl.equals(mlInput.h2oUrl);
    }

    @Override public int hashCode() {
        return Objects.hash(trainingData, predictionData, h2oUrl);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("MLInput").omitNullValues().add("trainingData", trainingData).add("predictionData", predictionData).add("h2oUrl",
                h2oUrl).toString();
    }
}
