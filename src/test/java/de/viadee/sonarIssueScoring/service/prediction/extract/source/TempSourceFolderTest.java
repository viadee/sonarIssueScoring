package de.viadee.sonarIssueScoring.service.prediction.extract.source;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.MoreFiles;

import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

public class TempSourceFolderTest {

    @Test public void contentIsWrittenAndDeleted() throws Exception {
        GitPath permanent = GitPath.of("folder/permanent.txt");
        GitPath added = GitPath.of("folder/added.txt");
        GitPath modified = GitPath.of("folder/modified.txt");
        GitPath deleted = GitPath.of("folder/deleted.txt");

        Path root;
        try (TempSourceFolder folder = new TempSourceFolder()) {
            root = folder.root();

            folder.update(ImmutableMap.of(permanent, "permanent", deleted, "deleted", modified, "mod1"));

            assertContent(permanent.toActualPath(root), "permanent");
            assertContent(modified.toActualPath(root), "mod1");
            assertContent(deleted.toActualPath(root), "deleted");


            folder.update(ImmutableMap.of(permanent, "permanent", modified, "mod2", added, "added"));

            assertFalse(Files.exists(deleted.toActualPath(root)));
            assertContent(permanent.toActualPath(root), "permanent");
            assertContent(modified.toActualPath(root), "mod2");
            assertContent(added.toActualPath(root), "added");
        }
        assertFalse(Files.exists(root));
    }

    private static void assertContent(Path p, String content) throws IOException {
        assertEquals(content, MoreFiles.asCharSource(p, Charset.defaultCharset()).read());
    }
}
