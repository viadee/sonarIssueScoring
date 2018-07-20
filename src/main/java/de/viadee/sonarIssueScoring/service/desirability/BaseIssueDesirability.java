package de.viadee.sonarIssueScoring.service.desirability;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

/**
 * The desirability of an issue is the desire / need of a software development team for it to be fixed.
 * <p>
 * This class contains the final Desirability-Score for a specific issue, as well as all its individual ratings to explain it
 */
@JsonSerialize
@Immutable
@ImmutableStyle
public abstract class BaseIssueDesirability {
    public abstract ImmutableList<Rating> ratings();

    public abstract double desirabilityScore();
}
