package de.viadee.sonarIssueScoring.service.prediction.load;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;
import org.immutables.value.Value.Immutable;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.Map;

/**
 * Represents a single commit in a repository.
 */
@Immutable
@ImmutableStyle
public abstract class BaseCommit {
    public abstract String id();

    public abstract String message();

    public abstract String authorEmail();

    /** Local time of author, from 0 to 1, expressed as fraction of the day */
    public abstract double authorTime();

    public abstract DayOfWeek authorDay();

    public abstract Map<Path, DiffType> diffs();

    public enum DiffType {
        ADDED,
        MODIFIED,
        DELETED
    }

    @Override public String toString() {
        return "Commit[" + id().substring(0, 10) + "]";
    }
}
