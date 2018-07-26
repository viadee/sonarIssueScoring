package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import static com.google.common.base.Preconditions.*;

import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithName;
import com.github.javaparser.ast.type.Type;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.ImmutableSetMultimap.Builder;
import com.google.common.collect.SetMultimap;

/**
 * Given a parsed project, tries to resolve all dependencies internally.
 * <p>
 * This is a simpler alternative to symbolSolver from https://github.com/javaparser/javaparser, which requires the whole class path (including dependencies) to be present
 * <p>
 * For instance, package resolution is built here: *
 * https://github.com/javaparser/javaparser/blob/master/javaparser-symbol-solver-core/src/main/java/com/github/javaparser/symbolsolver/javaparsermodel/contexts/CompilationUnitContext.java#L132
 */
class ProjectDependencyResolver {
    private final SetMultimap<String, Path> sourcePathsByFQN;
    private final SetMultimap<String, Path> sourcePathsBySimpleName;

    ProjectDependencyResolver(ParsedProject project) {
        Builder<String, Path> builderFQN = ImmutableSetMultimap.builder();
        Builder<String, Path> builderSimple = ImmutableSetMultimap.builder();
        project.all().forEach((path, cu) -> cu.findAll(TypeDeclaration.class).forEach(declaration -> {
            buildFullyQualifiedName(declaration).ifPresent(fqn -> builderFQN.put(fqn, path));
            builderSimple.put(declaration.getNameAsString(), path);
        }));
        sourcePathsByFQN = builderFQN.build();
        sourcePathsBySimpleName = builderSimple.build();
    }

    public FileDependencyInfo resolveAllDependencies(CompilationUnit cu) {
        Set<String> wildcardImports = extractWildcardImports(cu);
        Map<String, String> normalImports = extractFQImports(cu);

        DependencyTracker tracker = new DependencyTracker();

        extractReferencedSimpleTypes(cu).forEach(simpleType -> { // For each referenced simple type name
            if (normalImports.containsKey(simpleType)) { //Simplest and most common case: variable is imported normally.
                String fqn = normalImports.get(simpleType);
                tracker.addInternalOrExternal(sourcePathsByFQN.get(fqn)); // If the FQN exists in the project, its an internal, otherwise an external dependency
            } else { //No exact import found, try project-internal wildcard imports
                for (String wildcardImport : wildcardImports) {
                    Set<Path> internalPaths = sourcePathsByFQN.get(wildcardImport + "." + simpleType);
                    if (!internalPaths.isEmpty()) { //Found a matching name for a wildcard import
                        tracker.addInternalDependency(internalPaths);
                        return; //Only one wildcard should match
                    }
                }
                //No wildcard was matching
                //At this point, the simple name is neither imported normally, nor a project-known wildcard import
                //As a fallback, check if the simple name exists in the project, if so use it, otherwise its external
                tracker.addInternalOrExternal(sourcePathsBySimpleName.get(simpleType));
            }
        });

        //Add all normal imports as dependencies as well, as expressions like Runtime.getRuntime are not found by the construct above
        normalImports.values().stream().map(sourcePathsByFQN::get).forEach(tracker::addInternalOrExternal);

        return tracker.toDependencyInformation();
    }

    @VisibleForTesting
    static Set<String> extractReferencedSimpleTypes(CompilationUnit cu) {
        return cu.findAll(Type.class).stream().flatMap(type -> extractSimpleTypes(type).stream()).collect(ImmutableSet.toImmutableSet());
    }

    /** @return All packages names imported with a wildcard */
    @VisibleForTesting
    static Set<String> extractWildcardImports(CompilationUnit cu) {
        Set<String> wildcardImports = new HashSet<>(); // Package names

        wildcardImports.add(""); //The default package is always "imported"
        cu.getPackageDeclaration().ifPresent(pkg -> wildcardImports.add(pkg.getNameAsString())); // The current package is essentially a wildcard import

        cu.getImports().stream().filter(imp -> !imp.isStatic() && imp.isAsterisk()).forEach(imp -> wildcardImports.add(imp.getNameAsString()));

        return ImmutableSet.copyOf(wildcardImports);
    }

    /** @return Map from simple name to all matching fully qualified imports */
    @VisibleForTesting
    static Map<String, String> extractFQImports(CompilationUnit cu) {
        return cu.getImports().stream().filter(imp -> !imp.isAsterisk() && !imp.isStatic()).collect(
                ImmutableMap.toImmutableMap(imp -> imp.getName().getIdentifier(), imp -> imp.getName().asString(), (a, b) -> a)); //Just pick one in case of two imports for the same simple name
    }

    /**
     * Given a type, extract the simple name of all subtypes
     * <p>
     * For instance, a List&lt;A extends Comparable & CharSequence&gt; would yield [List, Comparable, CharSequence]
     * <p>
     * Some types, such as lambda parameters or var statements are unresolvable, and not returned
     * Primitives and the void type are excluded as well.
     */
    @VisibleForTesting
    static Set<String> extractSimpleTypes(Type referencedType) {
        Set<String> result = new HashSet<>();

        referencedType.ifUnionType(ut -> ut.getElements().forEach(inner -> result.addAll(extractSimpleTypes(inner))));

        referencedType.ifIntersectionType(it -> it.getElements().forEach(inner -> result.addAll(extractSimpleTypes(inner))));

        referencedType.ifClassOrInterfaceType(cit -> result.add(cit.getNameAsString()));

        referencedType.ifArrayType(at -> result.addAll(extractSimpleTypes(at.getComponentType())));

        referencedType.ifTypeParameter(tp -> tp.getTypeBound().forEach(bound -> result.addAll(extractSimpleTypes(bound))));

        referencedType.ifWildcardType(wc -> {
            wc.getExtendedType().ifPresent(extended -> result.addAll(extractSimpleTypes(extended)));
            wc.getSuperType().ifPresent(superType -> result.addAll(extractSimpleTypes(superType)));
        });
        //ignore UnknownType, VarType, VoidType, PrimitiveType
        return Collections.unmodifiableSet(result);
    }

    /** Walk up the parents to find the fully qualified name, as described in https://github.com/javaparser/javaparser/issues/222 */
    @VisibleForTesting
    static Optional<String> buildFullyQualifiedName(TypeDeclaration<?> type) {
        Node parent = type.getParentNode().orElseThrow(IllegalStateException::new);
        if (parent instanceof CompilationUnit)
            return Optional.of(((CompilationUnit) parent).getPackageDeclaration().map(NodeWithName::getNameAsString).orElse("") + "." + type.getNameAsString());
        if (parent instanceof TypeDeclaration)
            return buildFullyQualifiedName((TypeDeclaration<?>) parent).map(name -> name + "." + type.getNameAsString());
        return Optional.empty(); //Class defined in a method: class X { void m() { class Y { } } } (See LocalClassDeclarationStmt)
    }

    private static class DependencyTracker {
        private int internalDependencies, externalDependencies;
        private final Set<Path> referencedInternalPaths = new HashSet<>();

        /** If the given set is empty, its an external dependency. Otherwise its internal. */
        public void addInternalOrExternal(Set<Path> internallyReferencedFiles) {
            if (internallyReferencedFiles.isEmpty()) {
                addExternalDependency();
            } else {
                addInternalDependency(internallyReferencedFiles);
            }
        }

        public void addInternalDependency(Set<Path> internallyReferencedFiles) {
            checkState(!internallyReferencedFiles.isEmpty());
            internalDependencies++;
            referencedInternalPaths.addAll(internallyReferencedFiles);
        }

        public void addExternalDependency() { externalDependencies++;}

        public FileDependencyInfo toDependencyInformation() { return FileDependencyInfo.of(externalDependencies, internalDependencies, referencedInternalPaths);}
    }
}
