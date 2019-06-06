package de.viadee.sonarIssueScoring.service;

import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;

import de.viadee.sonarIssueScoring.service.desirability.ServerInfo;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public final class PredictionParams {
    private final ServerInfo gitServer;
    private final int predictionHorizon;

    private PredictionParams(ServerInfo gitServer, int predictionHorizon) {
        this.gitServer = Objects.requireNonNull(gitServer, "gitServer");
        this.predictionHorizon = predictionHorizon;
    }

    public static PredictionParams of(ServerInfo gitServer, int predictionHorizon) {
        return new PredictionParams(gitServer, predictionHorizon);
    }

    public ServerInfo gitServer() {
        return gitServer;
    }

    public int predictionHorizon() {
        return predictionHorizon;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        PredictionParams that = (PredictionParams) o;
        return predictionHorizon == that.predictionHorizon && gitServer.equals(that.gitServer);
    }

    @Override public int hashCode() {
        return Objects.hash(gitServer, predictionHorizon);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("gitServer", gitServer).add("predictionHorizon", predictionHorizon).toString();
    }
}
