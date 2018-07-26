package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import java.nio.file.Path;

import org.immutables.value.Value.Immutable;

import com.google.common.collect.ImmutableSet;

import de.viadee.sonarIssueScoring.misc.ImmutableStyle;

/**
 * Represents information on the dependencies of a single source code file
 */
@Immutable
@ImmutableStyle
public interface BaseFileDependencyInfo {
    /** number of external dependencies (not defined in the currently parsed project) */
    public int externalDependencies();

    /** internal dependencies (defined in the currently parsed project) */
    public int internalDependencies();

    /** All internal source files which are referenced by this class */
    public ImmutableSet<Path> referencedSourceFiles();
}
