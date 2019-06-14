package de.viadee.sonarIssueScoring.service.prediction;

import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
@JsonSerialize
public final class PredictionResult {
    private final ModelMetrics validationMetrics;
    private final ImmutableMap<GitPath, FileInformation> results;

    private PredictionResult(ModelMetrics validationMetrics, Map<GitPath, FileInformation> results) {
        this.validationMetrics = Objects.requireNonNull(validationMetrics, "validationMetrics");
        this.results = ImmutableMap.copyOf(results);
    }

    public static PredictionResult of(ModelMetrics validationMetrics, Map<GitPath, FileInformation> results) {
        return new PredictionResult(validationMetrics, results);
    }

    @JsonProperty public ModelMetrics validationMetrics() {
        return validationMetrics;
    }

    @JsonProperty public ImmutableMap<GitPath, FileInformation> results() {
        return results;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PredictionResult that = (PredictionResult) o;
        return validationMetrics.equals(that.validationMetrics) && results.equals(that.results);
    }

    @Override public int hashCode() {
        return Objects.hash(validationMetrics, results);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("PredictionResult").omitNullValues().add("validationMetrics", validationMetrics).add("results", results).toString();
    }
}
