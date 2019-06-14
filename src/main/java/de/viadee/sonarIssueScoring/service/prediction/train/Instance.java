package de.viadee.sonarIssueScoring.service.prediction.train;

import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckReturnValue;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import de.viadee.sonarIssueScoring.service.prediction.load.GitPath;

@ParametersAreNonnullByDefault
@Immutable
@CheckReturnValue
public class Instance {
    public static final String NAME_TARGET = "target", NAME_FOLD = "fold", NAME_RANDOM = "random", NAME_DEPENDANTS = "dependants";

    private final GitPath path;
    private final ImmutableMap<String, Object> data;

    private Instance(GitPath path, ImmutableMap<String, Object> data) {
        this.path = path;
        this.data = data;
    }

    public static Instance of(GitPath path, Map<String, Object> data) {
        return new Instance(path, ImmutableMap.copyOf(data));
    }

    public GitPath path() {return path;}

    public int fold() {
        return (Integer) data.get(NAME_FOLD);
    }

    public Double target() {
        return (Double) data.get(NAME_TARGET);
    }

    public int dependants() {return (Integer) data.get(NAME_DEPENDANTS);}

    public ImmutableMap<String, Object> data() {return data;}

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Instance instance = (Instance) o;
        return path.equals(instance.path) && data.equals(instance.data);
    }

    @Override public int hashCode() {
        return Objects.hash(path, data);
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("path", path).add("data", data).toString();
    }
}
