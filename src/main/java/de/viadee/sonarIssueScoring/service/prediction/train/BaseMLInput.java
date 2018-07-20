package de.viadee.sonarIssueScoring.service.prediction.train;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

import java.util.List;

@Immutable
@ImmutableStyle
public abstract class BaseMLInput {
    public abstract List<Instance> trainingData();

    public abstract List<Instance> predictionData();

    public abstract String h2oUrl();
}
