package de.viadee.sonarissuescoring.service.prediction.extract;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Maps;

import de.viadee.sonarissuescoring.service.prediction.load.Commit;
import de.viadee.sonarissuescoring.service.prediction.load.GitPath;

/** Base class for extracting the first / last part of a Filename */
public abstract class AbstractPrefixSuffixExtractor implements FeatureExtractor {
    private static final String NONE = "<none>";
    private static final CharMatcher uppercase = CharMatcher.inRange('A', 'Z');

    /**
     * Extracts the Prefix and Suffix of the filename
     * <p>
     * If a prefix / suffix is used by only one file, it is replaced with IsUnique instead.
     * If it is otherwise used less then 7 times, it is labeled as NonSpecial
     */
    @Override public void extractFeatures(List<Commit> commits, Output out) {
        Set<GitPath> paths = commits.stream().flatMap(c -> c.content().keySet().stream()).collect(Collectors.toSet());
        Map<GitPath, String> pathToExtracted = Maps.toMap(paths, p -> extractRelevantPart(p.fileName()));
        ImmutableMultiset<String> histogram = ImmutableMultiset.copyOf(pathToExtracted.values());

        commits.forEach(commit -> commit.content().forEach((path, content) -> {
            int count = histogram.count(pathToExtracted.get(path));
            out.add(commit, path, featureName(), count == 1 ? "IsUnique" : count < 5 ? "NonSpecial" : pathToExtracted.get(path));
        }));
    }

    protected abstract String extractRelevantPart(String filename);

    protected abstract String featureName();

    @Component
    public static class SuffixExtractor extends AbstractPrefixSuffixExtractor {
        @Override protected String extractRelevantPart(String filename) {
            int i = uppercase.lastIndexIn(filename);
            return i == -1 ? NONE : filename.substring(i, filename.length() - 5);
        }

        @Override protected String featureName() {
            return "suffix";
        }
    }

    @Component
    public static class PrefixExtractor extends AbstractPrefixSuffixExtractor {
        @Override protected String extractRelevantPart(String filename) {
            int i = uppercase.indexIn(filename, 1);
            return i == -1 ? NONE : filename.substring(0, i);
        }

        @Override protected String featureName() {
            return "prefix";
        }
    }
}
