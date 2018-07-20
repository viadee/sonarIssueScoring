package de.viadee.sonarIssueScoring.service.prediction.load;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

import java.nio.file.Path;
import java.util.Map;

/**
 * Represents the content of a repository at a specific point in time
 */
@ImmutableStyle
@Immutable
public abstract class BaseRepositorySnapshot {
    public abstract Commit snapshotCommit();

    public abstract Map<Path, String> getAllFiles();

    @Override public String toString() {
        return "RepositorySnapshot{" + getAllFiles().size() + " Files}";
    }
}
