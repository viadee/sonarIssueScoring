package de.viadee.sonarIssueScoring.service.prediction.load;

import com.google.common.collect.ImmutableList;
import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

/**
 * Main class representing the parsed content of a GithubRepo at a specific point in time
 */
@Immutable
@ImmutableStyle
public abstract class BaseRepo {
    public abstract ImmutableList<Commit> commits();

    /** Current content, as of time of snapshot */
    public abstract RepositorySnapshot snapshot();
}
