package de.viadee.sonarIssueScoring.service.prediction;

import java.nio.file.Path;

import org.immutables.value.Value.Immutable;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableMap;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;

@JsonSerialize
@Immutable
@ImmutableStyle
public abstract class BasePredictionResult {
    public abstract ModelMetrics validationMetrics();

    public abstract ImmutableMap<Path, FileInformation> results();
}
