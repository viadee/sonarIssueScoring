package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.MoreFiles;

public class TempSourceFolderTest {
    @Test
    public void contentIsWrittenAndDeleted() throws Exception {
        ImmutableMap<Path, String> content = ImmutableMap.of(Paths.get("folder/file.txt"), "content");

        Path root;
        try (TempSourceFolder folder = new TempSourceFolder(content)) {
            root = folder.root();
            Assert.assertEquals("content", MoreFiles.asCharSource(folder.root().resolve("folder").resolve("file.txt"), Charset.defaultCharset()).read());
        }
        Assert.assertFalse(Files.exists(root));
    }
}