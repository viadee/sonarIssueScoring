package de.viadee.sonarIssueScoring.service.prediction.extract;

import static com.google.common.base.Preconditions.*;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import com.github.mauricioaniche.ck.metric.CBO;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;

import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;

/**
 * Extracts some features related to CK-Metrics out of the files, by parsing their content.
 */
@Component
public class MetricExtractor implements FeatureExtractor {
    private static final Path DEPENDENCY_NOT_RESOLVED = Paths.get("VIRTUAL");

    static {
        System.setProperty("jdt.max", "100");

        //This new PrintStream is here to silence System.out.println in CBO.java on line 172

        @SuppressWarnings("squid:S106")
        PrintStream original = System.out;
        System.setOut(new PrintStream(original) {
            @Override
            public void println(Object x) {
                //noinspection ThrowableNotThrown
                if (!new Exception().getStackTrace()[1].getClassName().equals(CBO.class.getName()))
                    super.println(x);
            }
        });
    }

    @Override
    public void extractFeatures(Repo repo, Map<Path, Builder> output) {
        try (TempSourceFolder dir = new TempSourceFolder(repo.snapshot().getAllFiles())) {

            SetMultimap<String, String> dependencies = HashMultimap.create();
            Multiset<String> commentsPerFile = HashMultiset.create(); //String is filename

            CK ck = new CK();

            ck.plug(() -> new DependencyVisitor(dependencies, commentsPerFile)); // ck is single-threaded, this is safe.
            CKReport report = ck.calculate(dir.root().toString());


            output.forEach((path, out) -> out.numberOfComments(commentsPerFile.count(dir.root().resolve(path))));

            addOOMMetrics(output, dir.root(), report);
            addGraphMetrics(guavaDependencies(dependencies, report, dir.root()), output);
        } catch (Exception e) { //Catching all Exceptions because CK likes to throw exceptions like UnsupportedOperationException on some input
            throw new RuntimeException("Could not extract metrics for " + repo, e);
        }
    }

    private static void addOOMMetrics(Map<Path, Builder> output, Path basePath, CKReport data) {
        output.forEach((path, out) -> {
            CKNumber num = data.get(basePath.resolve(path).toString());
            if (num != null) {
                out.packageDef(packageName(num.getClassName())).
                        numberOfMethods(num.getNom()).
                        cyclomaticComplexity(num.getLcom()).
                        linesOfCode(num.getLoc());
            }
        });
    }

    private static String packageName(String className) {
        int i = className.lastIndexOf('.');
        return i == -1 ? "" : className.substring(0, i);
    }

    private static void addGraphMetrics(Graph<Path> dependencies, Map<Path, Builder> output) {
        output.forEach((path, out) -> {
            if (!dependencies.nodes().contains(path)) { //Happens if a class is mostly empty, such as an annotation without meta-annotations
                out.dependants(0).dependenciesProject(0).dependenciesExternal(0);
            } else {
                out.dependants(dependencies.inDegree(path));
                int nonResolved = (int) dependencies.successors(path).stream().filter(p -> p.startsWith(DEPENDENCY_NOT_RESOLVED)).count();
                out.dependenciesExternal(nonResolved);
                out.dependenciesProject(dependencies.outDegree(path) - nonResolved);
            }
        });
    }

    /** Build a file dependency graph. Contains virtual files for classes not in the input data */
    private static Graph<Path> guavaDependencies(SetMultimap<String, String> classNameDependencies, CKReport ckReport, Path basePath) {
        HashMultimap<String, Path> classNameToPaths = HashMultimap.create(); //Class name to all files its declared in (might be multiple, if there are multiple source roots
        ckReport.all().forEach(num -> classNameToPaths.put(num.getClassName(), basePath.relativize(Paths.get(num.getFile()))));

        MutableGraph<Path> dependencies = GraphBuilder.<Path>directed().allowsSelfLoops(true).build();
        classNameDependencies.forEach((from, to) -> {
            Set<Path> fromPaths = classNameToPaths.get(from);
            Set<Path> toPaths = classNameToPaths.get(to);

            checkState(!fromPaths.isEmpty());
            if (toPaths.isEmpty())
                toPaths.add(DEPENDENCY_NOT_RESOLVED.resolve(to)); //Dependency to an external class (such as java.lang). Add a virtual file for it to count in statistics

            fromPaths.forEach(fromPath -> toPaths.forEach(toPath -> dependencies.putEdge(fromPath, toPath)));
        });

        classNameToPaths.values().forEach(dependencies::addNode);

        return ImmutableGraph.copyOf(dependencies);
    }
}
