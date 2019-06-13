package de.viadee.sonarIssueScoring.service.prediction.extract.source;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import de.viadee.sonarIssueScoring.service.prediction.train.Instance;

public class DependencyGraph {
    private final MutableGraph<String> dependencies = GraphBuilder.<Path>directed().allowsSelfLoops(true).build();
    private final Map<Path, String> pathToClass = new HashMap<>();
    private final SetMultimap<String, Path> classesToPaths = HashMultimap.create();

    public void addClass(Path path, String clazz) {
        pathToClass.put(path, clazz);
        classesToPaths.put(clazz, path);
        dependencies.addNode(clazz);
    }

    public void addDependency(String from, String to) {
        dependencies.putEdge(from, to);
    }

    public void removeClass(Path path) {
        String clazz = pathToClass.remove(path);

        //Avoid ConcurrentModification
        Iterable<String> successors = new ArrayList<>(dependencies.successors(clazz));
        successors.forEach(out -> dependencies.removeEdge(clazz, out));
    }

    public void putMetrics(Path path, BiConsumer<String, Object> out) {
        String clazz = pathToClass.get(path);

        out.accept(Instance.NAME_DEPENDANTS, dependencies.predecessors(clazz).size());

        Set<String> classDependencies = dependencies.successors(clazz);
        long presentDependencies = classDependencies.stream().filter(classesToPaths::containsKey).count();

        out.accept("dependenciesProject", presentDependencies);
        out.accept("dependenciesExternal", classDependencies.size() - presentDependencies);
    }
}
