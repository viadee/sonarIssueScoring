package de.viadee.sonarIssueScoring.service.prediction.load;

import static com.google.common.collect.ImmutableList.toImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import de.viadee.sonarIssueScoring.service.PredictionParams;

/**
 * Loads a git repository from the cache, and creates snapshots at different points in time
 */
@Service
public class RepositoryLoader {
    private final RepositoryCache repositoryCache;
    private final CommitHistoryReader commitHistoryReader;
    private final CommitHistorySplitter commitHistorySplitter;

    public RepositoryLoader(RepositoryCache repositoryCache, CommitHistoryReader commitHistoryReader, CommitHistorySplitter commitHistorySplitter) {
        this.repositoryCache = repositoryCache;
        this.commitHistoryReader = commitHistoryReader;
        this.commitHistorySplitter = commitHistorySplitter;
    }

    public SplitRepository loadSplitRepository(PredictionParams params, SnapshotStrategy multiSampling) {
        try {
            return repositoryCache.readRepository(params.gitServer(), git -> {
                List<Commit> history = commitHistoryReader.readCommits(git.getRepository());

                Repo completePast = commitHistorySplitter.createRepo(git.getRepository(), history);

                List<Integer> offsets = multiSampling.generateOffsets(params.predictionHorizon()).limit(10).boxed().collect(toImmutableList());

                List<PastFuturePair> trainingData = new ArrayList<>();
                for (Integer offset : offsets) {
                    if (offset + params.predictionHorizon() >= history.size())
                        break; //No more history - we are done
                    trainingData.add(commitHistorySplitter.splitCommits(git.getRepository(), history, offset, params.predictionHorizon()));
                }

              return   SplitRepository.of(completePast, trainingData);
            });
        } catch (IOException e) {
            throw new RuntimeException("Error while reading " + params, e);
        }
    }
}
