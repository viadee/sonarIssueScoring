package de.viadee.sonarIssueScoring.service.prediction.extract;

import static com.google.common.base.Preconditions.*;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import com.github.mauricioaniche.ck.metric.CBO;
import com.google.common.collect.*;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableGraph;

import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance;

/**
 * Extracts some features related to CK-Metrics out of the files, by parsing their content.
 */
@Component
public class MetricExtractor implements FeatureExtractor {
    private static final Path DEPENDENCY_NOT_RESOLVED = Paths.get("VIRTUAL");

    static {
        System.setProperty("jdt.max", "100");

        //This new PrintStream is here to silence System.out.println in CBO.java on line 172

        @SuppressWarnings("squid:S106") PrintStream original = System.out;
        System.setOut(new PrintStream(original) {
            @Override public void println(Object x) {
                if (!new Exception().getStackTrace()[1].getClassName().equals(CBO.class.getName()))
                    super.println(x);
            }
        });
    }

    @Override public void extractFeatures(Repo repo, Output out) {
        try (TempSourceFolder dir = new TempSourceFolder(repo.currentContent())) {

            SetMultimap<String, String> dependencies = HashMultimap.create();
            Multiset<String> commentsPerFile = HashMultiset.create(); //String is filename

            CK ck = new CK();

            ck.plug(() -> new DependencyVisitor(dependencies, commentsPerFile)); // ck is single-threaded, this is safe.
            CKReport report = ck.calculate(dir.root().toString());

            out.add("numberOfComments", path -> commentsPerFile.count(dir.root().resolve(path).toString()));
            createOOMMetrics(out, dir.root(), report);
            createGraphMetrics(guavaDependencies(dependencies, report, dir.root(), out.paths()), out);
        } catch (Exception e) { //Catching all Exceptions because CK likes to throw exceptions like UnsupportedOperationException on some input
            throw new RuntimeException("Could not extract metrics for " + repo, e);
        }
    }

    private static void createOOMMetrics(Output out, Path basePath, CKReport data) {
        out.add(path -> {
            CKNumber num = data.get(basePath.resolve(path).toString());
            num = num != null ? num : new CKNumber(null, "", null);
            return ImmutableMap.of(//
                    "package", packageName(num.getClassName()),//
                    "numberOfMethods", num.getNom(),//
                    "cyclomaticComplexity", num.getWmc(),//
                    "linesOfCode", num.getLoc());
        });
    }

    private static String packageName(String className) {
        int i = className.lastIndexOf('.');
        return i == -1 ? "" : className.substring(0, i);
    }

    private static void createGraphMetrics(Graph<Path> dependencies, Output out) {
        out.add(path -> {
            int nonResolved = (int) dependencies.successors(path).stream().filter(p -> p.startsWith(DEPENDENCY_NOT_RESOLVED)).count();

            return ImmutableMap.of(//
                    Instance.NAME_DEPENDANTS, dependencies.inDegree(path),//
                    "dependenciesExternal", nonResolved,//
                    "dependenciesProject", dependencies.outDegree(path) - nonResolved);
        });
    }

    /** Build a file dependency graph. Contains virtual files for classes not in the input data */
    private static Graph<Path> guavaDependencies(SetMultimap<String, String> classNameDependencies, CKReport ckReport, Path basePath, Set<Path> relevant) {
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

        //Some classes might neither be referenced nor have references
        relevant.forEach(dependencies::addNode);

        return ImmutableGraph.copyOf(dependencies);
    }
}
