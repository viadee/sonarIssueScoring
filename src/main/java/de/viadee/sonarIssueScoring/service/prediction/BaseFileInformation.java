package de.viadee.sonarIssueScoring.service.prediction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

@Immutable
@JsonSerialize
@ImmutableStyle
public abstract class BaseFileInformation {
    public abstract double predictedChangeCount();

    public abstract double dependentCount();
}
