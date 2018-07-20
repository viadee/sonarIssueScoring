package de.viadee.sonarIssueScoring.service.prediction.train;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import de.viadee.sonarIssueScoring.service.desirability.ServerInfo;
import org.immutables.value.Value.Immutable;

@ImmutableStyle
@Immutable
@JsonDeserialize
public interface BaseFilePredictionParams {
    public abstract int predictionHorizon();

    public abstract ServerInfo gitServer();

    public abstract String h2oUrl();
}
