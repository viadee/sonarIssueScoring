package de.viadee.sonarIssueScoring.service.prediction;

import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public final class EvaluationResult {
    private final double rmse;
    private final double r2;
    private final ImmutableTable<Boolean, Boolean, Integer> confusionMatrix;

    private EvaluationResult(double rmse, double r2, Table<Boolean, Boolean, Integer> confusionMatrix) {
        this.rmse = rmse;
        this.r2 = r2;
        this.confusionMatrix = ImmutableTable.copyOf(confusionMatrix);
    }

    public static EvaluationResult of(double rmse, double r2, Table<Boolean, Boolean, Integer> confusionMatrix) {
        return new EvaluationResult(rmse, r2, confusionMatrix);
    }

    public double rmse() {
        return rmse;
    }

    public double r2() {
        return r2;
    }

    /** Rows: actual value > 80% percentile, Cols: predicted value > 80% percentile */
    public ImmutableTable<Boolean, Boolean, Integer> confusionMatrix() {
        return confusionMatrix;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EvaluationResult that = (EvaluationResult) o;
        return Double.compare(that.rmse, rmse) == 0 && Double.compare(that.r2, r2) == 0 && confusionMatrix.equals(that.confusionMatrix);
    }

    @Override public int hashCode() {
        return Objects.hash(rmse, r2, confusionMatrix);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("EvaluationResult").omitNullValues().add("rmse", rmse).add("r2", r2).add("confusionMatrix", confusionMatrix).toString();
    }
}
