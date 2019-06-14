package de.viadee.sonarIssueScoring.service.prediction.load;

import static de.viadee.sonarIssueScoring.service.prediction.load.Commit.DiffType.*;
import static org.eclipse.jgit.diff.DiffEntry.ChangeType;
import static org.eclipse.jgit.diff.DiffEntry.scan;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import javax.annotation.Nullable;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevFlag;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.EmptyTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit.DiffType;

/**
 * Transforms a JGit Repository in a linearized history of commits
 */
@Service
class CommitHistoryReader {
    private final TreeFilterSource treeFilterSource;
    private final RepositorySnapshotCreator snapshotCreator;

    CommitHistoryReader(TreeFilterSource treeFilterSource, RepositorySnapshotCreator snapshotCreator) {
        this.treeFilterSource = treeFilterSource;
        this.snapshotCreator = snapshotCreator;
    }

    /**
     * Reads all commits to the currently active (->master) branch.
     * Any commit not done on the master branch is excluded, only their merge commits are included
     * <pre>
     *     |
     *     * Commit D (included)
     *     |
     *     * Merge commit C (included)
     *     | \
     *     |  * Commit (excluded)
     *     |  |
     *     *  | Commit B (included)
     *     | /
     *     * Commit A (included)
     *     |
     * </pre>
     *
     * Results in the list [A, B, C, D]
     */
    public List<Commit> readCommits(Repository repo) throws IOException {
        List<Commit> history = new ArrayList<>();

        //This implementation walks backwards from the head, the list is revered immediately before returning
        try (RevWalk revWalk = new RevWalk(repo)) {
            RevFlag flagMainBranch = revWalk.newFlag("mainBranch");

            revWalk.markStart(repo.parseCommit(repo.resolve("HEAD")));
            //Children before parent commits -> newer commits first.
            // use: true is used to add to instead of replacing the currentSnapshot soring mechanism
            revWalk.sort(RevSort.TOPO);

            //We go trough the commits backwards, starting with the HEAD
            // => the loop variable is the previous commit for the comparision
            RevCommit current = null;
            for (RevCommit previous : revWalk) {
                if (current == null)
                    previous.add(flagMainBranch);

                //We are only interested in historical facts - simply walk back the main branch.
                //Merge commits contain the branch merged as first parent, any other parent is ignored.
                if (previous.getParentCount() > 0 && previous.has(flagMainBranch))
                    previous.getParent(0).add(flagMainBranch);

                if (!previous.has(flagMainBranch))
                    continue; //Non-Main commit, ignore

                if (current != null)
                    createCommitWithDiff(repo, current, previous).ifPresent(history::add);
                current = previous;
            }
            if (current != null) //Create a difference to the initially empty repo
                createCommitWithDiff(repo, current, null).ifPresent(history::add);
        }
        //Reverse the commits, so the initial commit is at position 0
        return ImmutableList.copyOf(history).reverse();
    }

    /** Creates a Commit object (containing a diff to the previous commit). If the previous commit is null, an empty tree will be used instead */
    @VisibleForTesting Optional<Commit> createCommitWithDiff(Repository repo, RevCommit current, @Nullable RevCommit previous) throws IOException {
        Map<GitPath, DiffType> diffs = createDiffMap(repo, current, previous);
        if (diffs.isEmpty())
            return Optional.empty();  //We are using commit counts in the prediction, so ignore non-java commits entirely
        Map<GitPath, String> snapshot = snapshotCreator.createSnapshot(repo, current);

        // https://stackoverflow.com/questions/11856983/why-git-authordate-is-different-from-commitdate
        // Committer time: Time the commit was last "modified", such as rebased
        // Author time: Time the original commit was made. Does not change. This is used.
        PersonIdent author = current.getAuthorIdent();

        OffsetDateTime commitTime = author.getWhen().toInstant().atOffset(ZoneOffset.ofTotalSeconds(author.getTimeZoneOffset() * 60));
        double partOfDay = (commitTime.getHour() * 60 + commitTime.getMinute()) / 1440.0;

        return Optional.of(Commit.of(current.getId().name(), current.getFullMessage(), author.getEmailAddress(), partOfDay, commitTime.getDayOfWeek(), diffs, snapshot));
    }

    /** Creates a Commit object (containing a diff to the previous commit). If the previous commit is null, an empty tree will be used instead */
    private Map<GitPath, DiffType> createDiffMap(Repository repo, RevCommit current, @Nullable RevCommit previous) throws IOException {
        try (TreeWalk treeWalk = new TreeWalk(repo)) {
            if (previous == null)
                treeWalk.addTree(new EmptyTreeIterator());
            else
                treeWalk.addTree(previous.getTree());
            treeWalk.addTree(current.getTree());
            treeWalk.setFilter(treeFilterSource.getTreeFilter());
            treeWalk.setRecursive(true); //We don't care about directories

            List<DiffEntry> diffEntries = scan(treeWalk);

            Map<GitPath, DiffType> diffs = new HashMap<>();

            for (DiffEntry diff : diffEntries) {
                if (diff.getChangeType() == ChangeType.ADD)
                    diffs.put(GitPath.of(diff.getNewPath()), ADDED);
                else if (diff.getChangeType() == ChangeType.MODIFY)
                    diffs.put(GitPath.of(diff.getNewPath()), MODIFIED);
                else if (diff.getChangeType() == ChangeType.DELETE)
                    diffs.put(GitPath.of(diff.getOldPath()), DELETED);
                else
                    throw new RuntimeException("Unexpected diff type: " + diff);
            }
            return diffs;
        }
    }
}
