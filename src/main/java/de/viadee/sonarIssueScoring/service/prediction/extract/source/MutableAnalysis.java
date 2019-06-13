package de.viadee.sonarIssueScoring.service.prediction.extract.source;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiConsumer;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import de.viadee.sonarIssueScoring.service.prediction.load.Commit;
import de.viadee.sonarIssueScoring.service.prediction.load.Commit.DiffType;

// Maybe implement WorkingCopyOwner as a mutable file system
class MutableAnalysis implements Closeable {
    private final TempSourceFolder sourceFolder = new TempSourceFolder();
    private final Table<Path, String, Object> currentAnalysis = HashBasedTable.create();
    private final DependencyGraph dependencies = new DependencyGraph();

    MutableAnalysis() throws IOException {}

    public void update(Commit c) throws IOException {
        sourceFolder.update(c.content());
        removeOld(c);
        parseModified(c);
    }

    private void removeOld(Commit c) {
        c.diffs().forEach((path, type) -> {
            if (type == DiffType.DELETED) {
                currentAnalysis.rowMap().remove(path);
                dependencies.removeClass(path);
            }
        });
    }

    private void parseModified(Commit c) {
        ASTParser parser = ASTParser.newParser(AST.JLS11);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(true);

        Map<String, String> options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_11, options);
        parser.setCompilerOptions(options);

        parser.setEnvironment(null, new String[]{sourceFolder.root().toString()}, new String[]{"UTF-8"}, true);

        String[] srcs = c.diffs().entrySet().stream().filter(e -> e.getValue() != Commit.DiffType.DELETED).map(
                e -> sourceFolder.root().resolve(e.getKey()).toString()).toArray(String[]::new);

        AdaptedMetricExecutor executor = new AdaptedMetricExecutor(sourceFolder.root(), currentAnalysis, dependencies);
        parser.createASTs(srcs, null, new String[0], executor, null);
    }

    @Override public void close() throws IOException {
        sourceFolder.close();
    }

    public void addAnalysis(Path path, BiConsumer<String,Object> out) {
        currentAnalysis.row(path).forEach(out::accept);
        dependencies.putMetrics(path, out);
    }
}
