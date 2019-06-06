package de.viadee.sonarIssueScoring.service.prediction;

import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
@JsonSerialize
public final class FileInformation {
    private final double predictedChangeCount;
    private final double dependentCount;

    private FileInformation(double predictedChangeCount, double dependentCount) {
        this.predictedChangeCount = predictedChangeCount;
        this.dependentCount = dependentCount;
    }

    public static FileInformation of(double predictedChangeCount, double dependentCount) {
        return new FileInformation(predictedChangeCount, dependentCount);
    }

    @JsonProperty public double predictedChangeCount() {
        return predictedChangeCount;
    }

    @JsonProperty public double dependentCount() {
        return dependentCount;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        FileInformation that = (FileInformation) o;
        return Double.compare(that.predictedChangeCount, predictedChangeCount) == 0 && Double.compare(that.dependentCount, dependentCount) == 0;
    }

    @Override public int hashCode() {
        return Objects.hash(predictedChangeCount, dependentCount);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("FileInformation").omitNullValues().add("predictedChangeCount", predictedChangeCount).add("dependentCount",
                dependentCount).toString();
    }
}
