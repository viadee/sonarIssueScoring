package de.viadee.sonarissuescoring.service.prediction;

import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
@JsonSerialize
public final class ModelMetrics {
    private final double rmse;
    private final double r2;
    private final double meanResidualDeviance;
    private final ImmutableMap<String, Double> variableImportances;

    private ModelMetrics(double rmse, double r2, double meanResidualDeviance, Map<String, Double> variableImportances) {
        this.rmse = rmse;
        this.r2 = r2;
        this.meanResidualDeviance = meanResidualDeviance;
        this.variableImportances = ImmutableMap.copyOf(variableImportances);
    }

    public static ModelMetrics of(double rmse, double r2, double meanResidualDeviance, Map<String, Double> variableImportances) {
        return new ModelMetrics(rmse, r2, meanResidualDeviance, variableImportances);
    }

    @JsonProperty public double rmse() {
        return rmse;
    }

    @JsonProperty public double r2() {
        return r2;
    }

    @JsonProperty public double meanResidualDeviance() {
        return meanResidualDeviance;
    }

    @JsonProperty public ImmutableMap<String, Double> variableImportances() {
        return variableImportances;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ModelMetrics that = (ModelMetrics) o;
        return Double.compare(that.rmse, rmse) == 0 && Double.compare(that.r2, r2) == 0 && Double.compare(that.meanResidualDeviance,
                meanResidualDeviance) == 0 && variableImportances.equals(that.variableImportances);
    }

    @Override public int hashCode() {
        return Objects.hash(rmse, r2, meanResidualDeviance, variableImportances);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("ModelMetrics").omitNullValues().add("rmse", rmse).add("r2", r2).add("meanResidualDeviance", meanResidualDeviance).add(
                "variableImportances", variableImportances).toString();
    }
}
