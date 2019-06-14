package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

/**
 * Extracts the age of the n-th last change to file = how many commits ago was the n-th last change.
 * <p>
 * Example: <br/>
 * Commit#4 (HEAD): Edited A.java, B.java<br/>
 * Commit#3: Edited A.java<br/>
 * Commit#2: Edited B.java<br/>
 * Commit#1: Edited C.java<br/>
 * <p>
 * CommitAge for C.java for the 0-th last commit: 3, as the first change to the files was 3 commits before the HEAD<br/>
 * CommitAge for B.Java for the 1-th last commit: 2
 */
public abstract class AbstractAgeExtractor implements FeatureExtractor {
    private static final int MAX_INDEX_LENGTH = Arrays.stream(CommitAge.values()).mapToInt(ca -> ca.offset + 1).max().orElse(-1);

    @Override public void extractFeatures(List<Commit> commits, Output out) {
        ListMultimap<GitPath, Integer> commitIndices = ArrayListMultimap.create(500, 4);

        for (int ci = 0; ci < commits.size(); ci++) {
            int commitIndex = ci;//Effectively final
            Commit commit = commits.get(commitIndex);

            commit.diffs().keySet().stream().map(this::extractPath).distinct().forEach(path -> {
                List<Integer> previousCommitIndices = commitIndices.get(path);
                previousCommitIndices.add(0, commitIndex);
                while (previousCommitIndices.size() > MAX_INDEX_LENGTH)
                    previousCommitIndices.remove(MAX_INDEX_LENGTH);
            });

            commit.content().keySet().forEach(path -> {
                List<Integer> previousCommitIndices = commitIndices.get(extractPath(path));
                for (CommitAge age : CommitAge.values()) {
                    Integer ageCommitIndex = Iterables.get(previousCommitIndices, age.offset, null);
                    out.add(commit, path, featureName(age), ageCommitIndex == null ? commits.size() + 1 : commitIndex - ageCommitIndex);
                }
            });
        }
    }

    protected abstract GitPath extractPath(GitPath input);

    protected abstract String featureName(CommitAge age);

    @Component
    public static class ClassAgeExtractor extends AbstractAgeExtractor {
        @Override protected GitPath extractPath(GitPath input) { return input;}

        @Override protected String featureName(CommitAge age) { return age.nameClass;}
    }

    @Component
    public static class PackageAgeExtractor extends AbstractAgeExtractor {
        @Override protected GitPath extractPath(GitPath input) { return input.dir();}

        @Override protected String featureName(CommitAge age) { return age.namePackage;}
    }

    /**
     * Represents an n-th last edit to a file
     * <p>
     * LastCommit is how many commits ago from the head a specific file was last changed
     */
    private enum CommitAge {
        LastCommit(0),
        Minus1(1),
        Minus2(2),
        Minus4(4),
        Minus8(8),
        Minus16(16);

        private final int offset;
        private final String nameClass = "commitAgeClass." + name();
        private final String namePackage = "commitAgePackage." + name();

        CommitAge(int offset) {
            this.offset = offset;
        }
    }
}
