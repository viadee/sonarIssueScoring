package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.util.List;

import com.google.common.collect.*;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Table.Cell;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance;

/**
 * Mutable class to accumulate features for a repository
 */
public class Output {
    private final Table<Commit, GitPath, Builder<String, Object>> data = HashBasedTable.create();

    public Output(List<Commit> commits) {
        commits.forEach(commit -> commit.content().keySet().forEach(path -> data.put(commit, path, ImmutableMap.builder())));
    }

    public void add(Commit c, GitPath p, String name, Object val) {
        data.get(c, p).put(name, val);
    }

    public Multimap<Commit, Instance> build() {
        return data.cellSet().stream().
                collect(ImmutableListMultimap.toImmutableListMultimap(
                        Cell::getRowKey,
                        c -> Instance.of(c.getColumnKey(), c.getValue().build())));
    }
}
