package de.viadee.sonarIssueScoring.service.prediction.load;

import java.nio.file.Path;

import org.immutables.value.Value.Immutable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;

/**
 * Main class representing the parsed content of a GithubRepo at a specific point in time
 */
@Immutable
@ImmutableStyle
public abstract class BaseRepo {
    /** All commits up until the snapshot */
    public abstract ImmutableList<Commit> commits();

    public abstract ImmutableMap<Path, String> currentContent();
}
