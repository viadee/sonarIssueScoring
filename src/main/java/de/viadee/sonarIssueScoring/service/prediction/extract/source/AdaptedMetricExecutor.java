package de.viadee.sonarIssueScoring.service.prediction.extract.source;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jdt.core.dom.CompilationUnit;

import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.MetricsExecutor;
import com.github.mauricioaniche.ck.metric.NOM;
import com.github.mauricioaniche.ck.metric.WMC;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;

public class AdaptedMetricExecutor extends MetricsExecutor {
    private final Path root;
    private final Table<Path, String, Object> output;
    private final DependencyGraph dependencyGraph;

    public AdaptedMetricExecutor(Path root, Table<Path, String, Object> output, DependencyGraph dependencyGraph) {
        super(() -> ImmutableList.of(new WMC(), new NOM(), new DependencyVisitor(dependencyGraph)));
        this.root = root;
        this.output = output;
        this.dependencyGraph = dependencyGraph;
    }

    @Override public void acceptAST(String sourceFilePath, CompilationUnit cu) {
        super.acceptAST(sourceFilePath, cu);

        CKNumber num = getReport().get(sourceFilePath);

        Path path = root.relativize(Paths.get(sourceFilePath));

        if (num == null) //Just guessing the classname
            num = new CKNumber(null, path.getFileName().toString().replace(".java", ""), null);

        dependencyGraph.addClass(path, num.getClassName());

        output.put(path, "numberOfComments", cu.getCommentList().size());
        output.put(path, "package", packageName(num.getClassName()));
        output.put(path, "numberOfMethods", num.getNom());
        output.put(path, "cyclomaticComplexity", num.getWmc());
        output.put(path, "linesOfCode", num.getLoc());
    }

    private static String packageName(String className) {
        int i = className.lastIndexOf('.');
        return i == -1 ? "" : className.substring(0, i);
    }
}
