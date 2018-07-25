package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import java.nio.file.Path;
import java.util.Set;

import com.github.javaparser.ast.body.TypeDeclaration;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;

/**
 * Resolve paths by the simple class name
 * <p>
 * An alternative would be to use the symbolSolver from https://github.com/javaparser/javaparser. It however seems to require the complete class path to be present
 */
class SimpleTypeLookup {
    private final ImmutableSetMultimap<String, Path> simpleTypeToSourcePath;

    SimpleTypeLookup(ParsedProject project) {
        Builder<String, Path> builder = ImmutableSetMultimap.builder();

        project.all().forEach((path, cu) -> cu.findAll(TypeDeclaration.class).forEach(type -> builder.put(type.getNameAsString(), path)));

        simpleTypeToSourcePath = builder.build();
    }

    /**
     * Given a type name in source code, return any potential matching source path in the project.
     * <p>
     * If the returned Set is empty, the referenced type is probably an external reference
     */
    public Set<Path> getSourcePaths(String simpleTypeDef) {
        return simpleTypeToSourcePath.get(simpleTypeDef);
    }
}
