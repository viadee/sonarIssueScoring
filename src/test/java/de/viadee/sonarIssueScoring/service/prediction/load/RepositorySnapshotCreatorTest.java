package de.viadee.sonarIssueScoring.service.prediction.load;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Paths;
import java.time.DayOfWeek;

public class RepositorySnapshotCreatorTest {
    @Rule public final TemporaryFolder folder = new TemporaryFolder();

    @SuppressWarnings("ResultOfMethodCallIgnored") private RevCommit commit(Git git, String file, String content) throws Exception {
        File f = new File(folder.getRoot(), file);
        f.getParentFile().mkdirs();
        f.createNewFile();
        Files.write(content.getBytes(), f);
        git.add().addFilepattern(".").call();
        return git.commit().setMessage(file).call();
    }

    @Test public void createSnapshot() throws Exception {
        try (Git git = Git.init().setDirectory(folder.getRoot()).call()) {
            commit(git, "ide.java", "ide");
            commit(git, "sata/is/superior.java", "superior");
            String commitId = commit(git, "no/jumper/needed.txt", "jumper").name();
            commit(git, "this/file/is/ignored.java", "ignored");


            Commit commit = Commit.of(commitId, "", "", 0, DayOfWeek.FRIDAY, ImmutableMap.of());

            RepositorySnapshot expected = RepositorySnapshot.of(commit, ImmutableMap.of(Paths.get("ide.java"), "ide", Paths.get("sata/is/superior.java"), "superior"));
            RepositorySnapshot read = new RepositorySnapshotCreator(new TreeFilterSource(), new StringCache()).createSnapshot(git.getRepository(), commit);
            Assert.assertEquals(expected, read);
        }
    }
}