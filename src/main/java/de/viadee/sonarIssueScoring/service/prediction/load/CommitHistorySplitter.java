package de.viadee.sonarIssueScoring.service.prediction.load;

import org.eclipse.jgit.lib.Repository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Creates a repo for a specific range in time
 */
@Service
class CommitHistorySplitter {
    private final RepositorySnapshotCreator repositorySnapshotCreator;

    CommitHistorySplitter(RepositorySnapshotCreator repositorySnapshotCreator) { this.repositorySnapshotCreator = repositorySnapshotCreator;}

    /**
     * Splits a commit list in past and future. Commits start with HEAD, and go in the past
     * The breakpoint commit at which the prediction is done (last commit of past) is pastOffset + predictionHorizon. This is the last commit in the past.
     * Ranges:
     * 0                              .. pastOffset (excl): Ignored
     * pastOffset                     .. pastOffset + predictionHorizon (excl): Future
     * pastOffset + predictionHorizon .. end (incl): Past
     */
    public PastFuturePair splitCommits(Repository repository, List<Commit> commits, int pastOffset, int predictionHorizon) throws IOException {
        int breakpoint = pastOffset + predictionHorizon;

        Repo futureRepo = createRepo(repository, commits.subList(pastOffset, breakpoint));
        Repo pastRepo = createRepo(repository, commits.subList(breakpoint, commits.size()));

        return PastFuturePair.of(pastRepo, futureRepo);
    }

    public Repo createRepo(Repository repo, List<Commit> commits) throws IOException {
        return Repo.of(commits, repositorySnapshotCreator.createSnapshot(repo, commits.get(0)));
    }
}
