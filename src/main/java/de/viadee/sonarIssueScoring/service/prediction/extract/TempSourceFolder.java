package de.viadee.sonarIssueScoring.service.prediction.extract;

import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.*;

/**
 * Represents a source folder by writing the supplied map of file path and file content to a temporary directory.
 * Deletes it on closing.
 */
class TempSourceFolder implements AutoCloseable {
    private final Path root;

    public TempSourceFolder(Map<Path, String> content) throws IOException {
        root = Files.createTempDirectory("TempSourceFolder");


        //Write files to actual filesystem
        for (Entry<Path, String> e : content.entrySet()) {
            Path actualPath = root.resolve(e.getKey());

            checkState(actualPath.startsWith(root));

            Path parent = actualPath.getParent();
            Files.createDirectories(parent);
            Files.write(actualPath, e.getValue().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE_NEW);
        }
    }

    public Path root() {return root;}

    @Override public void close() throws Exception {
        MoreFiles.deleteRecursively(root, RecursiveDeleteOption.ALLOW_INSECURE);
    }
}
