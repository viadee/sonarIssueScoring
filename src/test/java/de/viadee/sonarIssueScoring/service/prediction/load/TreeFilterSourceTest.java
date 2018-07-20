package de.viadee.sonarIssueScoring.service.prediction.load;

import com.google.common.collect.ImmutableSet;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TreeFilterSourceTest {
    @Rule public final TemporaryFolder folder = new TemporaryFolder();

    @Test public void testFilter() throws Exception {
        //Included
        create("src/main/java/package/File.java");
        create("primary-tests/src/main/java/package/File.java");
        create("src/main/java/package-tests/File.java");
        //Excluded
        create("src/test/java/package/File.java");
        create("test/package/File.java");
        create("src/main/java/package/package-info.java");
        create("src/main/java/package/module-info.java");
        create("src/main/java/NonJava.txt");


        Set<String> found = new HashSet<>();
        try (Git git = Git.init().setDirectory(folder.getRoot()).call()) {
            git.add().addFilepattern(".").call();
            RevCommit commit = git.commit().setMessage("").call();

            try (TreeWalk treeWalk = new TreeWalk(git.getRepository())) {
                treeWalk.addTree(commit.getTree());
                treeWalk.setFilter(new TreeFilterSource().getTreeFilter());
                treeWalk.setRecursive(true);

                while (treeWalk.next())
                    found.add(treeWalk.getPathString());
            }
        }

        Assert.assertEquals(ImmutableSet.of("src/main/java/package/File.java", "primary-tests/src/main/java/package/File.java", "src/main/java/package-tests/File.java"),
                found);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") private void create(String file) throws IOException {
        File f = new File(folder.getRoot(), file);
        f.getParentFile().mkdirs();
        f.createNewFile();
    }
}