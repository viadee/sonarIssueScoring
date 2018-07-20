package de.viadee.sonarIssueScoring.service.prediction.load;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

/**
 * Data for instance create for the training: Contains a pair of past data and the corresponding predictable future data
 */
@Immutable
@ImmutableStyle
public interface BasePastFuturePair {
    public Repo past();

    public Repo future();
}
