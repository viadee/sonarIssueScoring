package de.viadee.sonarIssueScoring.service.prediction.extract;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.CommitAge;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

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
    @Override public void extractFeatures(Repo repo, Map<Path, Builder> output) {
        ListMultimap<Path, Integer> commitIndices = ArrayListMultimap.create(output.size(), 4);

        ImmutableList<Commit> commits = repo.commits();


        for (int i = 0; i < commits.size(); i++) {
            Integer iFinal = i;
            commits.get(i).diffs().keySet().stream().map(this::extractPath).distinct().forEach(path -> commitIndices.put(path, iFinal));
        }

        output.forEach((path, out) -> {
            List<Integer> commitList = commitIndices.get(extractPath(path));

            for (CommitAge age : CommitAge.values())
                putResult(out, age, Iterables.get(commitList, age.offset, Integer.MAX_VALUE));
        });
    }

    protected abstract Path extractPath(Path input);

    protected abstract void putResult(Builder out, CommitAge age, int result);

    @Component
    public static class ClassAgeExtractor extends AbstractAgeExtractor {
        @Override protected Path extractPath(Path input) { return input;}

        @Override protected void putResult(Builder out, CommitAge age, int result) { out.putCommitAge(age, result);}
    }

    @Component
    public static class PackageAgeExtractor extends AbstractAgeExtractor {
        @Override protected Path extractPath(Path input) { return input.getParent();}

        @Override protected void putResult(Builder out, CommitAge age, int result) { out.putCommitAgePackage(age, result);}
    }
}
