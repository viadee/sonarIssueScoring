package de.viadee.sonarIssueScoring.service.prediction.load;

import com.google.common.collect.ImmutableMap;
import de.viadee.sonarIssueScoring.service.prediction.load.BaseCommit.DiffType;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

public class CommitHistoryReaderTest {

    @Rule public final TemporaryFolder folder = new TemporaryFolder();

    @Test public void readCommits() throws Exception {
        try (Git git = Git.init().setDirectory(folder.getRoot()).call()) {
            commit(git, "0.java");
            RevCommit split = commit(git, "1.java");
            commit(git, "2.java");
            commit(git, "3.java");

            git.checkout().setCreateBranch(true).setName("branch").setStartPoint(split).call();
            commit(git, "5.java");

            git.checkout().setName("master").call();
            commit(git, "4.java");

            git.merge().include(git.getRepository().resolve("branch")).call();
            commit(git, "6.java");

            List<Commit> read = new CommitHistoryReader(new TreeFilterSource()).readCommits(git.getRepository());

            Assert.assertEquals(7, read.size());

            for (int i = read.size() - 1; i >= 0; i--) {
                Assert.assertEquals(ImmutableMap.of(Paths.get((read.size() - i - 1) + ".java"), DiffType.ADDED), read.get(i).diffs());
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") private RevCommit commit(Git git, String file) throws Exception {
        new File(folder.getRoot(), file).createNewFile();
        git.add().addFilepattern(".").call();
        return git.commit().setMessage(file).call();
    }
}