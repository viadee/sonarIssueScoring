package de.viadee.sonarIssueScoring.service.prediction.load;

import java.nio.file.Path;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;

import org.springframework.data.annotation.Immutable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.CharMatcher;

/**
 * Basically java.nio.file.path, but required because the windows implementation is case sensitive
 */
@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
@JsonSerialize
@JsonDeserialize
public class GitPath implements Comparable<GitPath> {
    private static final CharMatcher trimmer = CharMatcher.is('/');

    private final String path;
    private final String dir;
    private final String filename;

    @JsonCreator(mode = Mode.DELEGATING) private GitPath(String path) {
        this.path = trimmer.trimAndCollapseFrom(path.replace('\\', '/'), '/');
        this.filename = path.substring(path.lastIndexOf('/') + 1); // NotFound (-1) + 1 = 0
        this.dir = path.substring(0, path.length() - filename.length());
    }

    public static GitPath of(String path) {return new GitPath(path);}

    public static GitPath ofRealPath(Path root, Path actual) {
        return new GitPath(root.relativize(actual).toString());
    }

    @JsonValue public String path() {return path;}

    public GitPath dir() {return new GitPath(dir);}

    public String fileName() {
        return filename;
    }

    public Path toActualPath(Path root) {
        return root.resolve(path);
    }

    @Override public int compareTo(GitPath o) {
        return path.compareTo(o.path);
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        GitPath gitPath = (GitPath) o;
        return path.equals(gitPath.path);
    }

    @Override public int hashCode() {
        return path.hashCode();
    }

    @Override public String toString() {
        return path;
    }
}
