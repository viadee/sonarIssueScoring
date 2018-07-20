package de.viadee.sonarIssueScoring.service;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import de.viadee.sonarIssueScoring.service.desirability.ServerInfo;
import org.immutables.value.Value.Immutable;

@JsonSerialize
@Immutable
@ImmutableStyle
public abstract class BasePredictionParams {
    public abstract ServerInfo gitServer();

    public abstract int predictionHorizon();
}
