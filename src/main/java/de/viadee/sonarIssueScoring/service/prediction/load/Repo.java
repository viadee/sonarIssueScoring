package de.viadee.sonarIssueScoring.service.prediction.load;

import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Main class representing the parsed content of a GithubRepo at a specific point in time
 */
@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public final class Repo {
    private final ImmutableList<Commit> commits;
    private final ImmutableMap<Path, String> currentContent;

    private Repo(Iterable<Commit> commits, Map<Path, String> currentContent) {
        this.commits = ImmutableList.copyOf(commits);
        this.currentContent = ImmutableMap.copyOf(currentContent);
    }

    public static Repo of(Iterable<Commit> commits, Map<Path, String> currentContent) {
        return new Repo(commits, currentContent);
    }

    /**
     * All commits up until the snapshot
     */
    public ImmutableList<Commit> commits() {
        return commits;
    }

    public ImmutableMap<Path, String> currentContent() {
        return currentContent;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Repo repo = (Repo) o;
        return commits.equals(repo.commits) && currentContent.equals(repo.currentContent);
    }

    @Override public int hashCode() {
        return Objects.hash(commits, currentContent);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper("Repo").omitNullValues().add("commits", commits).add("currentContent", currentContent).toString();
    }
}
