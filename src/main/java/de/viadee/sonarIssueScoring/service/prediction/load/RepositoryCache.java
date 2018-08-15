package de.viadee.sonarIssueScoring.service.prediction.load;

import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.catalina.util.ServerInfo;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.StandardSystemProperty;
import com.google.common.hash.Hashing;

/**
 * Caches git repositories in java.io.tempdir
 *
 * It uses bare repositories to avoid potential merge conflicts.
 */
@Service
public class RepositoryCache {
	private static final Logger log = LoggerFactory.getLogger(RepositoryCache.class);
	private final Path tempDir;

	public RepositoryCache() {
		tempDir = Paths.get(StandardSystemProperty.JAVA_IO_TMPDIR.value());
	}

	public Git getRepository(ServerInfo gitServer) throws IOException, GitAPIException {
		final Path target = tempDir.resolve(extractRepositoryName(gitServer.url()));
		log.info("Using folder {} as cache for {}", target, gitServer.url());

		Git git = null;
		if (!Files.exists(target)) {
			log.info("Cache directory is missing - cloning remote repo {} to {}", gitServer.url(), target);
			git = addAuthIfRequired(
					Git.cloneRepository().setURI(gitServer.url()).setBare(true).setDirectory(target.toFile()),
					gitServer).call();
		} else {
			log.info("Directory is present - pulling to {}", target);
			try {
				git = Git.open(target.toFile());
				addAuthIfRequired(git.fetch(), gitServer).call();
			} catch (IOException | GitAPIException e) {
				if (git != null) // No TWR, as the object leaves the scope
					git.close();
				// No automatic deletion, as it is impossible to do so securely in absence of a
				// SecureDirectoryStream (see com.google.common.io.MoreFiles#deleteRecursively)
				// If this is considered secure regardless, beware that the pack files
				// (objects/pack/*) are readonly, and can't be deleted with NIO by default.
				throw new IOException("Exception during opening / pulling cache for " + gitServer.url()
						+ ", consider deleting the cache at " + target, e);
			} finally {
				git.close();
			}
		}

		log.info("Local repository is now up-to-date");
		return git;
	}

	private static <T extends TransportCommand<?, ?>> T addAuthIfRequired(T command, ServerInfo info) {
		if (info.user() != null && info.password() != null)
			command.setCredentialsProvider(new UsernamePasswordCredentialsProvider(info.user(), info.password()));
		return command;
	}

	/**
	 * Extracts a unique directory name from the repository url. Includes hash of
	 * url to make collisions unlikely
	 */
	@VisibleForTesting
	static String extractRepositoryName(String url) {
		final Matcher m = Pattern.compile(".*/([^/]+)/([^/]+)(\\.git)?").matcher(url);
		checkState(m.find(), "Could not extract repository name from %s", url);
		return "repo-" + m.group(1) + "." + m.group(2) + "." + Hashing.murmur3_128().hashUnencodedChars(url) + ".git";
	}
}
