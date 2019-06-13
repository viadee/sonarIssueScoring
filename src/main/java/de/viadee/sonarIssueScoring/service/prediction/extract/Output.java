package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.nio.file.Path;
import java.util.List;

import com.google.common.collect.*;
import com.google.common.collect.ImmutableMap.Builder;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance;

/**
 * Mutable class to accumulate features for a repository
 */
public class Output {
    private final Table<Commit, Path, Builder<String, Object>> data = HashBasedTable.create();

    public Output(List<Commit> commits) {
        commits.forEach(commit -> commit.content().keySet().forEach(path -> data.put(commit, path, ImmutableMap.builder())));
    }

    public Output add(Commit c, Path p, String name, Object val) {
        data.get(c, p).put(name, val);
        return this;
    }

    public Multimap<Commit, Instance> build() {
        return data.cellSet().stream().
                collect(ImmutableListMultimap.toImmutableListMultimap(
                        Table.Cell::getRowKey,
                        c -> Instance.of(c.getColumnKey(), c.getValue().build())));
    }
}
