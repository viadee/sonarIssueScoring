package de.viadee.sonarIssueScoring.service.prediction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

import java.util.Map;

@JsonSerialize
@ImmutableStyle
@Immutable
public abstract class BaseModelMetrics {
    public abstract double mse();

    public abstract double rmse();

    public abstract double r2();

    public abstract double meanResidualDeviance();

    public abstract double mae();

    public abstract double rmsle();

    public abstract Map<String, Double> variableImportances();
}
