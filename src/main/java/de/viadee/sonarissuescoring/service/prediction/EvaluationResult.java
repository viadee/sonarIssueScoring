package de.viadee.sonarissuescoring.service.prediction;

import java.util.List;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public final class EvaluationResult {
    private final ModelMetrics validationMetrics;
    private final double actualRmse;
    private final double actualR2;
    private final ImmutableTable<Boolean, Boolean, Integer> confusionMatrix;
    private final ImmutableList<EvaluationResultPath> evaluatedPaths;

    private EvaluationResult(ModelMetrics validationMetrics, double actualRmse, double actualR2, Table<Boolean, Boolean, Integer> confusionMatrix,
                             List<EvaluationResultPath> evaluatedPaths) {
        this.validationMetrics = validationMetrics;
        this.actualRmse = actualRmse;
        this.actualR2 = actualR2;
        this.confusionMatrix = ImmutableTable.copyOf(confusionMatrix);
        this.evaluatedPaths = ImmutableList.copyOf(evaluatedPaths);
    }

    public static EvaluationResult of(ModelMetrics validationMetrics, double actualRmse, double actualR2, Table<Boolean, Boolean, Integer> confusionMatrix,
                                      List<EvaluationResultPath> evaluatedPaths) {
        return new EvaluationResult(validationMetrics, actualRmse, actualR2, confusionMatrix, evaluatedPaths);
    }

    public ModelMetrics validationMetrics() {return validationMetrics;}

    public double actualRmse() {
        return actualRmse;
    }

    public double actualR2() {
        return actualR2;
    }

    public List<EvaluationResultPath> evaluatedPaths() {return evaluatedPaths;}

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
        return Double.compare(that.actualRmse, actualRmse) == 0 && Double.compare(that.actualR2, actualR2) == 0 && validationMetrics.equals(
                that.validationMetrics) && confusionMatrix.equals(that.confusionMatrix) && evaluatedPaths.equals(that.evaluatedPaths);
    }

    @Override public int hashCode() {
        return Objects.hash(validationMetrics, actualRmse, actualR2, confusionMatrix, evaluatedPaths);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("validationMetrics", validationMetrics).add("actualRmse", actualRmse).add("actualR2", actualR2).add("confusionMatrix",
                confusionMatrix).add("evaluatedPaths", evaluatedPaths).toString();
    }
}
