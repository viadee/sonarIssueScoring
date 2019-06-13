package de.viadee.sonarIssueScoring.service.prediction;

import java.nio.file.Path;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public class EvaluationResultPath {
    private final Path path;
    private final double predicted;
    private final double actual;

    private EvaluationResultPath(Path path, double predicted, double actual) {
        this.path = path;
        this.predicted = predicted;
        this.actual = actual;
    }

    public static EvaluationResultPath of(Path path, double predicted, double actual) {
        return new EvaluationResultPath(path, predicted, actual);
    }

    public Path path() { return path;}

    public double predicted() { return predicted;}

    public double actual() { return actual;}

    public double absError() {return Math.abs(predicted - actual);}

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EvaluationResultPath that = (EvaluationResultPath) o;
        return Double.compare(that.predicted, predicted) == 0 && Double.compare(that.actual, actual) == 0 && path.equals(that.path);
    }

    @Override public int hashCode() {
        return Objects.hash(path, predicted, actual);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("path", path).add("predicted", predicted).add("actual", actual).toString();
    }
}
