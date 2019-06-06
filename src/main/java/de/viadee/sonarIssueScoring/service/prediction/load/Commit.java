package de.viadee.sonarIssueScoring.service.prediction.load;

import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableMap;

/**
 * Represents a single commit in a repository.
 */
@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public final class Commit {
    private final String id;
    private final String message;
    private final String authorEmail;
    private final double authorTime;
    private final DayOfWeek authorDay;
    private final ImmutableMap<Path, DiffType> diffs;

    public enum DiffType {
        ADDED,
        MODIFIED,
        DELETED
    }

    private Commit(String id, String message, String authorEmail, double authorTime, DayOfWeek authorDay, Map<Path, DiffType> diffs) {
        this.id = Objects.requireNonNull(id, "id");
        this.message = Objects.requireNonNull(message, "message");
        this.authorEmail = Objects.requireNonNull(authorEmail, "authorEmail");
        this.authorTime = authorTime;
        this.authorDay = Objects.requireNonNull(authorDay, "authorDay");
        this.diffs = ImmutableMap.copyOf(diffs);
    }

    public static Commit of(String id, String message, String authorEmail, double authorTime, DayOfWeek authorDay, Map<Path, DiffType> diffs) {
        return new Commit(id, message, authorEmail, authorTime, authorDay, diffs);
    }

    public String id() {
        return id;
    }

    public String message() {
        return message;
    }

    public String authorEmail() {
        return authorEmail;
    }

    /**
     * Local time of author, from 0 to 1, expressed as fraction of the day
     */
    public double authorTime() {
        return authorTime;
    }

    public DayOfWeek authorDay() {
        return authorDay;
    }

    public ImmutableMap<Path, DiffType> diffs() {
        return diffs;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Commit commit = (Commit) o;
        return Double.compare(commit.authorTime, authorTime) == 0 && id.equals(commit.id) && message.equals(commit.message) && authorEmail.equals(
                commit.authorEmail) && authorDay == commit.authorDay && diffs.equals(commit.diffs);
    }

    @Override public int hashCode() {
        return Objects.hash(id, message, authorEmail, authorTime, authorDay, diffs);
    }

    @Override public String toString() {
        return "Commit[" + id().substring(0, 10) + "]";
    }
}
