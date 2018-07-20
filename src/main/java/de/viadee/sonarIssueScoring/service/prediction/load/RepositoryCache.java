package de.viadee.sonarIssueScoring.service.prediction.load;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.StandardSystemProperty;
import com.google.common.hash.Hashing;
import de.viadee.sonarIssueScoring.service.desirability.ServerInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.*;

/**
 * Caches git repositories in java.io.tempdir
 */
@Service
public class RepositoryCache {
    private static final Logger log = LoggerFactory.getLogger(RepositoryCache.class);
    private final Path tempDir;

    public RepositoryCache() {
        this.tempDir = Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value());
    }

    public Git getRepository(ServerInfo gitServer) throws IOException, GitAPIException {
        Path target = tempDir.resolve(extractRepositoryName(gitServer.url()));
        log.info("Using folder {} as cache for {}", target, gitServer.url());

        Git git;
        if (!Files.exists(target)) {
            log.info("Cache directory is missing - cloning remote repo {} to {}", gitServer.url(), target);
            git = addAuthIfRequired(Git.cloneRepository().setURI(gitServer.url()).setDirectory(target.toFile()), gitServer).call();
        } else {
            log.info("Directory is present - pulling to {}", target);
            git = Git.open(target.toFile());
            git.reset().setMode(ResetType.HARD).setRef(Constants.HEAD).call();
            addAuthIfRequired(git.pull().setStrategy(MergeStrategy.THEIRS), gitServer).call();
        }

        log.info("Local repository is now up-to-date");
        return git;
    }

    private static <T extends TransportCommand<?, ?>> T addAuthIfRequired(T command, ServerInfo info) {
        if (info.user() != null && info.password() != null)
            command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(info.user(), info.password()));
        return command;
    }

    /** Extracts an "unique" directory name from the repository url. Includes hash of url to make collisions unlikely */
    @VisibleForTesting static String extractRepositoryName(String url) {
        Matcher m = Pattern.compile(".*/([^/]+)/([^/]+)(\\.git)?").matcher(url);
        checkState(m.find(), "Could not extract repository name from %s", url);
        return "repo-" + m.group(1) + "." + m.group(2) + "." + Hashing.murmur3_128().hashUnencodedChars(url);
    }
}
