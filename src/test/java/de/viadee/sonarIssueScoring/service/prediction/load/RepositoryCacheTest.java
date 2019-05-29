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

    @Test public void testRepoCache() throws Exception {
        //Create "remote" repository to be cloned later
        try (Git remote = Git.init().setDirectory(folder.getRoot()).call()) {
            remote.commit().setAllowEmpty(true).setMessage("Commit 1").call();
            remote.checkout().setName("secondary").setCreateBranch(true).call(); // second branch, should not be used as head
            remote.commit().setAllowEmpty(true).setMessage("Commit 2").call();
            remote.checkout().setName("master").call();
        }
        //ServerInfo for the above repository
        ServerInfo remoteServerInfo = ServerInfo.anonymous(folder.getRoot().toURI().toString());

        //Clone the previously created repository
        List<RevCommit> cachedCommits = new RepositoryCache().readRepository(remoteServerInfo, cachedGit -> ImmutableList.copyOf(cachedGit.log().call()));
        Assert.assertEquals(1, cachedCommits.size());
        Assert.assertEquals("Commit 1", cachedCommits.get(0).getFullMessage());

        //Update "remote" repository
        try (Git remote = Git.open(folder.getRoot())) {
            remote.commit().setAllowEmpty(true).setMessage("Commit 3").call();
        }

        //Check that the update was mirrored too
        List<RevCommit> updatedCachedCommits = new RepositoryCache().readRepository(remoteServerInfo, cachedGit -> ImmutableList.copyOf(cachedGit.log().call()));
        Assert.assertEquals(2, updatedCachedCommits.size());
        Assert.assertEquals("Commit 3", updatedCachedCommits.get(0).getFullMessage());
        Assert.assertEquals("Commit 1", updatedCachedCommits.get(1).getFullMessage());
    }

    @Test public void extractRepositoryName() {
        Assert.assertEquals("repo-google.guava.2f3d28dec6073c346afa1061c8292c49.git", RepositoryCache.extractRepositoryName("https://github.com/google/guava"));
        Assert.assertEquals("repo-google.guava.4ddbb6a8541d64754b3f5cdef70812d6.git", RepositoryCache.extractRepositoryName("https://github.com/google/guava.git"));
        Assert.assertEquals("repo-repos.project.2ffa741792ed5a70f0a389e6fdb5fb48.git", RepositoryCache.extractRepositoryName("C:\\Users\\any\\repos\\project"));
        Assert.assertEquals("repo-repos.project.0c7fc628e678c63ea0185148f54d61f8.git", RepositoryCache.extractRepositoryName("C:\\Users\\any\\repos\\project\\.git"));
        Assert.assertEquals("repo-repos.project.a5e9e9653a15166112a8d2023f8138d5.git", RepositoryCache.extractRepositoryName("/home/any/repos/project"));
        Assert.assertEquals("repo-repos.project.f249a8207d7382dcdadf9fe4d45d8d2e.git", RepositoryCache.extractRepositoryName("/home/any/repos/project/.git"));
    }
}