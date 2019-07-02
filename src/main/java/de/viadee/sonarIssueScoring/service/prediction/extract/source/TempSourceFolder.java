package de.viadee.sonarIssueScoring.service.prediction.extract.source;

import static com.google.common.base.Preconditions.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.MoreFiles;
import com.google.common.io.RecursiveDeleteOption;

import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

/**
 * Represents a source folder by writing the supplied map of file path and file content to a temporary directory.
 * Deletes it on closing.
 */
class TempSourceFolder implements AutoCloseable {
    private final Path root;
    private Map<GitPath, String> previous = ImmutableMap.of();

    TempSourceFolder() throws IOException {
        root = Files.createTempDirectory("TempSourceFolder");
    }

    public void update(Map<GitPath, String> current) throws IOException {
        for (GitPath path : previous.keySet())
            if (!current.containsKey(path))
                Files.deleteIfExists(path.toActualPath(root)); //The file _should_ exist, but might not if there are two files only differing in capitalization on win systems

        for (Entry<GitPath, String> e : current.entrySet()) {

            if (e.getValue().equals(previous.get(e.getKey())))
                continue;

            Path actualPath = e.getKey().toActualPath(root);

            checkState(actualPath.startsWith(root));

            Path parent = actualPath.getParent();
            Files.createDirectories(parent);
            Files.write(actualPath, e.getValue().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
        }

        previous = ImmutableMap.copyOf(current);
    }

    public Path root() {return root;}

    @Override public void close() throws IOException {
        MoreFiles.deleteRecursively(root, RecursiveDeleteOption.ALLOW_INSECURE);
    }
}
