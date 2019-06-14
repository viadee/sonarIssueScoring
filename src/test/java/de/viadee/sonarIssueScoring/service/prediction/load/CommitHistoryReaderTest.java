package de.viadee.sonarIssueScoring.service.prediction.load;

import static de.viadee.sonarIssueScoring.service.prediction.load.Commit.DiffType.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableMap;

public class CommitHistoryReaderTest {
    private final RepositorySnapshotCreator snapshotCreator = new RepositorySnapshotCreator(new TreeFilterSource(), new StringCache());

    @Rule public final TemporaryFolder folder = new TemporaryFolder();

    @Test public void testFlattening() throws Exception {
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

            List<Commit> read = new CommitHistoryReader(new TreeFilterSource(), snapshotCreator).readCommits(git.getRepository());

            Assert.assertEquals(7, read.size());

            for (int i = 0; i < 7; i++) {
                Assert.assertEquals(ImmutableMap.of(GitPath.of(i + ".java"), ADDED), read.get(i).diffs());
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") private RevCommit commit(Git git, String file) throws Exception {
        new File(folder.getRoot(), file).createNewFile();
        git.add().addFilepattern(".").call();
        return git.commit().setMessage(file).call();
    }

    @Test public void testIgnoreNonJava() throws Exception {
        try (Git git = Git.init().setDirectory(folder.getRoot()).call()) {
            commit(git, "0.c");
            List<Commit> read = new CommitHistoryReader(new TreeFilterSource(), snapshotCreator).readCommits(git.getRepository());

            Assert.assertEquals(0, read.size());
        }
    }

    @Test public void testModifyDelete() throws Exception {
        try (Git git = Git.init().setDirectory(folder.getRoot()).call()) {
            commit(git, "1.java");
            //Modify
            Path file = folder.getRoot().toPath().resolve("1.java");
            Files.write(file, "abc".getBytes());
            git.add().addFilepattern(".").call();
            git.commit().setMessage("mod").call();
            //Delete
            Files.delete(file);
            git.rm().addFilepattern("1.java").call();
            git.commit().setMessage("del").call();

            List<Commit> read = new CommitHistoryReader(new TreeFilterSource(), snapshotCreator).readCommits(git.getRepository());

            Assert.assertEquals(3, read.size());
            Assert.assertEquals(ImmutableMap.of(GitPath.of("1.java"), ADDED), read.get(0).diffs());
            Assert.assertEquals(ImmutableMap.of(GitPath.of("1.java"), MODIFIED), read.get(1).diffs());
            Assert.assertEquals(ImmutableMap.of(GitPath.of("1.java"), DELETED), read.get(2).diffs());
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") @Test public void metaDataExtraction() throws Exception {
        try (Git git = Git.init().setDirectory(folder.getRoot()).call()) {
            //File is required, otherwise no java file is found and no commit is created
            new File(folder.getRoot(), "1.java").createNewFile();
            git.add().addFilepattern(".").call();

            //Monday January 12, 1970 22:46:39 (pm) in time zone Asia/Tokyo (JST)
            PersonIdent author = new PersonIdent("name", "email", new Date(999_999_000), TimeZone.getTimeZone("Asia/Tokyo"));
            RevCommit revCommit = git.commit().setMessage("msg").setAuthor(author).call();

            Commit c = new CommitHistoryReader(new TreeFilterSource(), snapshotCreator).createCommitWithDiff(git.getRepository(), revCommit, null).orElseThrow(
                    IllegalStateException::new);

            Assert.assertEquals("email", c.authorEmail());
            Assert.assertEquals(DayOfWeek.MONDAY, c.authorDay());
            Assert.assertEquals(0.9490625, c.authorTime(), 1.0e8);
            Assert.assertEquals("msg", c.message());
        }
    }
}
