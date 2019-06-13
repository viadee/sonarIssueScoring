package de.viadee.sonarIssueScoring.service.prediction.extract.source;

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.MoreFiles;

public class TempSourceFolderTest {

    @Test public void contentIsWrittenAndDeleted() throws Exception {
        Path permanent = Paths.get("folder/permanent.txt");
        Path added = Paths.get("folder/added.txt");
        Path modified = Paths.get("folder/modified.txt");
        Path deleted = Paths.get("folder/deleted.txt");

        Path root;
        try (TempSourceFolder folder = new TempSourceFolder()) {
            root = folder.root();

            folder.update(ImmutableMap.of(permanent, "permanent", deleted, "deleted", modified, "mod1"));

            assertContent(root.resolve(permanent), "permanent");
            assertContent(root.resolve(modified), "mod1");
            assertContent(root.resolve(deleted), "deleted");


            folder.update(ImmutableMap.of(permanent, "permanent", modified, "mod2", added, "added"));

            assertFalse(Files.exists(root.resolve(deleted)));
            assertContent(root.resolve(permanent), "permanent");
            assertContent(root.resolve(modified), "mod2");
            assertContent(root.resolve(added), "added");
        }
        assertFalse(Files.exists(root));
    }

    private static void assertContent(Path p, String content) throws IOException {
        Assert.assertEquals(content, MoreFiles.asCharSource(p, Charset.defaultCharset()).read());
    }
}
