package de.viadee.sonarIssueScoring.service.prediction.load;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

/**
 * Creates a snapshot of the repo content at a specific commit
 */
@Service
class RepositorySnapshotCreator {
    private static final Logger log = LoggerFactory.getLogger(RepositorySnapshotCreator.class);
    private final TreeFilterSource treeFilterSource;
    private final StringCache stringCache;

    RepositorySnapshotCreator(TreeFilterSource treeFilterSource, StringCache stringCache) {
        this.treeFilterSource = treeFilterSource;
        this.stringCache = stringCache;
    }

    public ImmutableMap<Path, String> createSnapshot(Repository repo, Commit commit) throws IOException {
        return createSnapshot(repo, repo.parseCommit(repo.resolve(commit.id())));
    }

    public ImmutableMap<Path, String> createSnapshot(Repository repo, RevCommit commitGit) throws IOException {
        log.trace("Creating snapshot for {}", commitGit);
        try (TreeWalk treeWalk = new TreeWalk(repo)) {
            treeWalk.addTree(commitGit.getTree());
            treeWalk.setFilter(treeFilterSource.getTreeFilter());
            treeWalk.setRecursive(true); //We don't care about directories - this automatically enters them

            Map<Path, String> content = new HashMap<>();

            while (treeWalk.next()) {
                //ObjectId 0 = objectId from the first (and only) tree
                //UTF-8 should fit most files, some encoding errors are ok.
                String fileContents = new String(repo.open(treeWalk.getObjectId(0)).getCachedBytes(), StandardCharsets.UTF_8);
                content.put(Paths.get(stringCache.deduplicate(treeWalk.getPathString())), stringCache.deduplicate(fileContents));
            }

            return ImmutableMap.copyOf(content);
        }
    }
}
