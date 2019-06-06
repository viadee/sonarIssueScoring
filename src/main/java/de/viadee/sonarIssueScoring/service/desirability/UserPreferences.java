package de.viadee.sonarIssueScoring.service.desirability;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * User preferences for the calculation of the Desirability-Score
 */
@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
@JsonDeserialize
public final class UserPreferences {
    private final ServerInfo sonarServer;
    private final String sonarProjectId;
    private final ImmutableMap<RatingType, Double> ratingWeights;
    private final ImmutableMap<Path, Double> directoryScores;
    private final int predictionHorizon;
    private final ServerInfo gitServer;
    private final String h2oUrl;

    private UserPreferences(ServerInfo sonarServer, String sonarProjectId, Map<RatingType, Double> ratingWeights, Map<Path, Double> directoryScores,
                            int predictionHorizon, ServerInfo gitServer, String h2oUrl) {
        this.sonarServer = Objects.requireNonNull(sonarServer, "sonarServer");
        this.sonarProjectId = Objects.requireNonNull(sonarProjectId, "sonarProjectId");
        this.ratingWeights = Maps.immutableEnumMap(ratingWeights);
        this.directoryScores = ImmutableMap.copyOf(directoryScores);
        this.predictionHorizon = predictionHorizon;
        this.gitServer = Objects.requireNonNull(gitServer, "gitServer");
        this.h2oUrl = Objects.requireNonNull(h2oUrl, "h2oUrl");
    }

    //Jackson deserializes null for missing maps
    @JsonCreator public static UserPreferences of(ServerInfo sonarServer, String sonarProjectId, @Nullable Map<RatingType, Double> ratingWeights,
                                                  @Nullable @JsonDeserialize(keyUsing = PathKeyDeserializer.class) Map<Path, Double> directoryScores,
                                                  int predictionHorizon, ServerInfo gitServer, String h2oUrl) {
        return new UserPreferences(sonarServer, sonarProjectId, ratingWeights == null ? Collections.emptyMap() : ratingWeights,
                directoryScores == null ? Collections.emptyMap() : directoryScores, predictionHorizon, gitServer, h2oUrl);
    }

    /**
     * server to fetch issues from
     */
    public ServerInfo sonarServer() {
        return sonarServer;
    }

    /**
     * @return The value of the {@code sonarProjectId} attribute
     */
    public String sonarProjectId() {
        return sonarProjectId;
    }

    /**
     * Individual weights for each RatingType to personalize. A weight of 0 disables a particular type
     */
    public ImmutableMap<RatingType, Double> ratingWeights() {
        return ratingWeights;
    }

    /**
     * Additional scores / ratings for specific subdirectories - the most specific directory wins
     */
    public ImmutableMap<Path, Double> directoryScores() {
        return directoryScores;
    }

    public int predictionHorizon() {
        return predictionHorizon;
    }

    public ServerInfo gitServer() {
        return gitServer;
    }

    public String h2oUrl() {
        return h2oUrl;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserPreferences that = (UserPreferences) o;
        return predictionHorizon == that.predictionHorizon && sonarServer.equals(that.sonarServer) && sonarProjectId.equals(that.sonarProjectId) && ratingWeights.equals(
                that.ratingWeights) && directoryScores.equals(that.directoryScores) && gitServer.equals(that.gitServer) && h2oUrl.equals(that.h2oUrl);
    }

    @Override public int hashCode() {
        return Objects.hash(sonarServer, sonarProjectId, ratingWeights, directoryScores, predictionHorizon, gitServer, h2oUrl);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("UserPreferences").omitNullValues().add("sonarServer", sonarServer).add("sonarProjectId", sonarProjectId).add("ratingWeights",
                ratingWeights).add("directoryScores", directoryScores).add("predictionHorizon", predictionHorizon).add("gitServer", gitServer).add("h2oUrl",
                h2oUrl).toString();
    }
}
