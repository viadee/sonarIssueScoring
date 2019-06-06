package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.ImmutableTable.Builder;

import de.viadee.sonarIssueScoring.service.prediction.train.Instance;

/**
 * Mutable class to accumulate features for a repository
 */
public class Output {

    private final ImmutableSet<Path> paths;
    private final Builder<Path, String, Object> data = ImmutableTable.builder();

    public Output(ImmutableSet<Path> paths) {
        this.paths = paths;
    }

    public Output add(String name, Function<Path, ?> generator) {
        paths.forEach(path -> data.put(path, name, generator.apply(path)));
        return this;
    }

    public Output add(Function<Path, Map<String, ?>> generator) {
        paths.forEach(path -> generator.apply(path).forEach((name, val) -> data.put(path, name, val)));
        return this;
    }

    public ImmutableSet<Path> paths() {return paths;}

    public List<Instance> build() {
        return data.build().rowMap().entrySet().stream().map(e -> Instance.of(e.getKey(), e.getValue())).collect(ImmutableList.toImmutableList());
    }
}
