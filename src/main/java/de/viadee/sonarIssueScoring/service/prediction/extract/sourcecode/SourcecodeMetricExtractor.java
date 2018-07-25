package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.type.Type;
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
    private final TypeSimplifier typeSimplifier;
    private final CyclomaticComplexityCalculator cyclomaticComplexityCalculator;

    public SourcecodeMetricExtractor(TypeSimplifier typeSimplifier, CyclomaticComplexityCalculator cyclomaticComplexityCalculator) {
        this.typeSimplifier = typeSimplifier;
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

    private void calculateGraphMetrics(ParsedProject project, Map<Path, Builder> output) {
        SimpleTypeLookup typeLookup = new SimpleTypeLookup(project);

        //All inter-project dependencies, from referenced to referencing file
        SetMultimap<Path, Path> dependants = HashMultimap.create();

        project.all().forEach((path, cu) -> {
            // All distinct simple names referenced by the current class
            Set<String> referencedSimpleNames = cu.findAll(Type.class).stream().flatMap(typeSimplifier::extractSimpleTypeNames).collect(toImmutableSet());

            //All internal files referenced by the above simple names
            //A single simple name can refer to multiple files, if it is ambiguous.
            List<Set<Path>> referencedPaths = referencedSimpleNames.stream().map(typeLookup::getSourcePaths).filter(paths -> !paths.isEmpty()).collect(toImmutableList());

            int projectDependencies = referencedPaths.size(); // Size of the list, not of the sum of inner sets. The relevant count is the count of files referencing internal files

            if (output.containsKey(path))
                output.get(path).dependenciesProject(projectDependencies).dependenciesExternal(referencedSimpleNames.size() - projectDependencies);

            referencedPaths.stream().flatMap(Collection::stream).forEach(referencedPath -> dependants.put(referencedPath, path));
        });

        output.forEach((path, out) -> out.dependants(dependants.get(path).size()));
    }
}
