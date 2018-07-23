package de.viadee.sonarIssueScoring.service.prediction.load;

import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.ImmutableList;

import de.viadee.sonarIssueScoring.service.desirability.ServerInfo;

public class RepositoryCacheTest {
    @Rule public final TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testRepoCache() throws Exception {
        //Create "remote" repository to be cloned later
        try (Git remote = Git.init().setDirectory(folder.getRoot()).call()) {
            remote.commit().setAllowEmpty(true).setMessage("Commit 1").call();
            remote.checkout().setName("secondary").setCreateBranch(true).call(); // second branch, should not be used as head
            remote.commit().setAllowEmpty(true).setMessage("Commit 2").call();
            remote.checkout().setName("master").call();
        }

        //Clone the previously created repository
        try (Git cachedGit = new RepositoryCache().getRepository(ServerInfo.anonymous(folder.getRoot().toURI().toString()))) {
            List<RevCommit> commits = ImmutableList.copyOf(cachedGit.log().call());
            Assert.assertEquals(1, commits.size());
            Assert.assertEquals("Commit 1", commits.get(0).getFullMessage());
        }

        //Update "remote" repository
        try (Git remote = Git.open(folder.getRoot())) {
            remote.commit().setAllowEmpty(true).setMessage("Commit 3").call();
        }

        //Check that the update was mirrored too
        try (Git cachedGit = new RepositoryCache().getRepository(ServerInfo.anonymous(folder.getRoot().toURI().toString()))) {
            List<RevCommit> commits = ImmutableList.copyOf(cachedGit.log().call());
            Assert.assertEquals(2, commits.size());
            Assert.assertEquals("Commit 3", commits.get(0).getFullMessage());
            Assert.assertEquals("Commit 1", commits.get(1).getFullMessage());
        }
    }

    @Test
    public void extractRepositoryName() {
        Assert.assertEquals("repo-google.guava.2f3d28dec6073c346afa1061c8292c49.git", RepositoryCache.extractRepositoryName("https://github.com/google/guava"));
    }
}