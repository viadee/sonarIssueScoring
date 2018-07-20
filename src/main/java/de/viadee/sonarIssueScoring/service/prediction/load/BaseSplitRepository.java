package de.viadee.sonarIssueScoring.service.prediction.load;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

import java.util.List;

/**
 * Main class for all training data, contains a number data pairs for model training, and the repository state to use for prediction with the final model
 */
@Immutable
@ImmutableStyle
public interface BaseSplitRepository {
    public Repo completePast();

    public List<PastFuturePair> trainingData();
}
