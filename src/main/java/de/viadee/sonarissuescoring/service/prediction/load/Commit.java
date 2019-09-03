package de.viadee.sonarissuescoring.service.prediction.load;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.collect.ImmutableMap;

/**
 * Represents a single commit in a repository.
 *
 * Note: Object equality is solely based on commit id
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
    private final ImmutableMap<GitPath, DiffType> diffs;
    private final ImmutableMap<GitPath, String> content;

    public enum DiffType {
        ADDED,
        MODIFIED,
        DELETED
    }

    private Commit(String id, String message, String authorEmail, double authorTime, DayOfWeek authorDay, Map<GitPath, DiffType> diffs, Map<GitPath, String> content) {
        this.id = Objects.requireNonNull(id, "id");
        this.message = Objects.requireNonNull(message, "message");
        this.authorEmail = Objects.requireNonNull(authorEmail, "authorEmail");
        this.authorTime = authorTime;
        this.authorDay = Objects.requireNonNull(authorDay, "authorDay");
        this.diffs = ImmutableMap.copyOf(diffs);
        this.content = ImmutableMap.copyOf(content);
    }

    public static Commit of(String id, String message, String authorEmail, double authorTime, DayOfWeek authorDay, Map<GitPath, DiffType> diffs, Map<GitPath, String> content) {
        return new Commit(id, message, authorEmail, authorTime, authorDay, diffs, content);
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

    public ImmutableMap<GitPath, DiffType> diffs() {
        return diffs;
    }

    /**
     * Current content of the complete repository
     */
    public ImmutableMap<GitPath, String> content() {
        return content;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Commit commit = (Commit) o;
        return Objects.equals(id, commit.id);
    }

    @Override public int hashCode() {
        return Objects.hash(id);
    }

    @Override public String toString() {
        return "Commit[" + id().substring(0, 10) + "]";
    }
}
