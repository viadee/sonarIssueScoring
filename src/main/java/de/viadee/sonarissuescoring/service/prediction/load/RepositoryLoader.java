package de.viadee.sonarissuescoring.service.prediction.load;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import de.viadee.sonarissuescoring.service.PredictionParams;

/**
 * Loads a git repository from the cache
 */
@Service
public class RepositoryLoader {
    private final RepositoryCache repositoryCache;
    private final CommitHistoryReader commitHistoryReader;

    public RepositoryLoader(RepositoryCache repositoryCache, CommitHistoryReader commitHistoryReader) {
        this.repositoryCache = repositoryCache;
        this.commitHistoryReader = commitHistoryReader;
    }

    public List<Commit> loadSplitRepository(PredictionParams params) {
        try {
            return repositoryCache.readRepository(params.gitServer(), git -> commitHistoryReader.readCommits(git.getRepository()));
        } catch (IOException e) {
            throw new RuntimeException("Error while reading " + params, e);
        }
    }
}
