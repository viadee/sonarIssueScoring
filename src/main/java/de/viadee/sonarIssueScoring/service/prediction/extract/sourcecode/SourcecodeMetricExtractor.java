package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.google.common.base.CharMatcher;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import de.viadee.sonarIssueScoring.service.prediction.extract.FeatureExtractor;
import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;

/**
 * Extracts some features related to CK-Metrics out of the files, by parsing their content.
 */
@Component
public class SourcecodeMetricExtractor implements FeatureExtractor {
    private static final CharMatcher newlineMatcher = CharMatcher.is('\n');
    private final CyclomaticComplexityCalculator cyclomaticComplexityCalculator;

    public SourcecodeMetricExtractor(CyclomaticComplexityCalculator cyclomaticComplexityCalculator) {
        this.cyclomaticComplexityCalculator = cyclomaticComplexityCalculator;
    }

    @Override
    public void extractFeatures(Repo repo, Map<Path, Builder> output) {
        ParsedProject project = new ParsedProject(repo.snapshot().getAllFiles());

        output.forEach((path, out) -> {
            CompilationUnit cu = project.get(path);

            out.packageDef(cu.getPackageDeclaration().map(pkg -> pkg.getName().asString()).orElse(""));
            out.numberOfMethods(cu.findAll(MethodDeclaration.class).size());
            out.numberOfComments(cu.findAll(Comment.class).size());
            out.cyclomaticComplexity(cyclomaticComplexityCalculator.cyclomaticComplexity(cu));

            out.linesOfCode(newlineMatcher.countIn(repo.snapshot().getAllFiles().get(path)));
        });

        calculateGraphMetrics(project, output);
    }

    private static void calculateGraphMetrics(ParsedProject project, Map<Path, Builder> output) {
        ProjectDependencyResolver resolver = new ProjectDependencyResolver(project);

        //All inter-project dependencies, from referenced to referencing file
        SetMultimap<Path, Path> dependants = HashMultimap.create();

        project.all().forEach((path, cu) -> {
            FileDependencyInfo fileDependencies = resolver.resolveAllDependencies(cu);

            if (output.containsKey(path))
                output.get(path).dependenciesProject(fileDependencies.internalDependencies()).dependenciesExternal(fileDependencies.externalDependencies());

            for (Path referenced : fileDependencies.referencedSourceFiles()) {
                dependants.put(referenced, path);
            }
        });

        output.forEach((path, out) -> out.dependants(dependants.get(path).size()));
    }
}
