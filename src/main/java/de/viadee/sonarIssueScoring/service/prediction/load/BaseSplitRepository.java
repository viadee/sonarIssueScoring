package de.viadee.sonarIssueScoring.service.prediction.load;

import java.util.List;

import org.immutables.value.Value.Immutable;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;

/**
 * Main class for all training data, contains a number data pairs for model training, and the repository state to use for prediction with the final model
 */
@Immutable
@ImmutableStyle
public interface BaseSplitRepository {
    /** complete repository past for final prediction */
    public Repo completePast();

    /** base data for instance extraction */
    public List<PastFuturePair> trainingData();
}
