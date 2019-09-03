package de.viadee.sonarissuescoring.service.prediction.extract;

import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;

import java.util.List;

import com.google.common.collect.*;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Table.Cell;

import de.viadee.sonarissuescoring.service.prediction.load.Commit;
import de.viadee.sonarissuescoring.service.prediction.load.GitPath;
import de.viadee.sonarissuescoring.service.prediction.train.Instance;

/**
 * Mutable class to accumulate features for a repository
 */
public class Output {
    private Table<Commit, GitPath, Builder<String, Object>> data = HashBasedTable.create();

    public Output(List<Commit> commits) {
        commits.forEach(commit -> commit.content().keySet().forEach(path -> data.put(commit, path, ImmutableMap.builder())));
    }

    public void add(Commit c, GitPath p, String name, Object val) {
        data.get(c, p).put(name, val);
    }

    /**
     * Build the data. For RAM reasons, this can be done only once.
     */
    public Multimap<Commit, Instance> build() {
        checkNotNull(data, "Already built");

        ImmutableListMultimap.Builder<Commit, Instance> ret = ImmutableListMultimap.builder();


        Multimap<Commit, Instance> out = Streams.stream(Iterables.consumingIterable(data.cellSet())).collect(
                toImmutableListMultimap(Cell::getRowKey, c -> Instance.of(c.getColumnKey(), c.getValue().build())));

        data = null; //Mark the data as disposed and release the memory
        return out;
    }
}
