package de.viadee.sonarIssueScoring.service.desirability;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

import java.nio.file.Path;
import java.util.Map;

/**
 * User preferences for the calculation of the Desirability-Score
 */
@Immutable
@ImmutableStyle
@JsonDeserialize
@JsonSerialize
public abstract class BaseUserPreferences {
    /** server to fetch issues from */
    public abstract ServerInfo sonarServer();

    public abstract String sonarProjectId();

    /** Individual weights for each RatingType to personalize. A weight of 0 disables a particular type */
    public abstract Map<RatingType, Double> ratingWeights();

    /** Additional scores / ratings for specific subdirectories - the most specific directory wins */
    @JsonDeserialize(keyUsing = PathKeyDeserializer.class) public abstract Map<Path, Double> directoryScores();

    public abstract int predictionHorizon();

    public abstract ServerInfo gitServer();

    public abstract String h2oUrl();
}
