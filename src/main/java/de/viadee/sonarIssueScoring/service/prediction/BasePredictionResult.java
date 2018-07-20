package de.viadee.sonarIssueScoring.service.prediction;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

import java.nio.file.Path;

@JsonSerialize
@Immutable
@ImmutableStyle
public abstract class BasePredictionResult {
    public abstract ModelMetrics metrics();

    public abstract ImmutableMap<Path, FileInformation> results();
}
