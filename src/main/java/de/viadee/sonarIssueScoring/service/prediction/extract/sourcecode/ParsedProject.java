package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import static com.google.common.base.Preconditions.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.ImmutableMap;

/**
 * Contains all files for a given repository state, parsed as CompilationUnits
 */
class ParsedProject {
    private static final Logger log = LoggerFactory.getLogger(ParsedProject.class);
    private final ImmutableMap<Path, CompilationUnit> parsedFiles;

    ParsedProject(Map<Path, String> fileContent) {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(LanguageLevel.BLEEDING_EDGE);
        config.setStoreTokens(false);

        JavaParser parser = new JavaParser(config);

        parsedFiles = fileContent.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, e -> parse(parser, e.getKey(), e.getValue())));
    }

    private static CompilationUnit parse(JavaParser parser, Path path, String content) {
        ParseResult<CompilationUnit> res = parser.parse(ParseStart.COMPILATION_UNIT, Providers.provider(content));

        if (res.isSuccessful())
            return res.getResult().get(); //Optional has to be present
        return generateFallbackClass(path);
    }

    /**
     * To avoid having to handle parse failures separately, this methods creates a simplistic compilation unit, as if the class was empty except for a single class definition
     */
    private static CompilationUnit generateFallbackClass(Path path) {
        log.warn("Could not parse {}, using empty fallback class", path);

        CompilationUnit cu = new CompilationUnit();
        cu.addClass(path.getFileName().toString().replace(".java", ""));
        return cu;
    }

    public ImmutableMap<Path, CompilationUnit> all() { return parsedFiles;}

    public CompilationUnit get(Path path) {return checkNotNull(parsedFiles.get(path), "No CU for %s", path);}
}
