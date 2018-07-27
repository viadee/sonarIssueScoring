package de.viadee.sonarIssueScoring.service.prediction;

import org.immutables.value.Value.Immutable;

import com.google.common.collect.Table;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;

@Immutable
@ImmutableStyle
public interface BaseEvaluationResult {
    public double rmse();

    public double r2();

    /** Rows: actual value > 80% percentile, Cols: predicted value > 80% percentile */
    public Table<Boolean, Boolean, Integer> confusionMatrix();
}
