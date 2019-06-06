package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.nio.file.Path;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Maps;

import de.viadee.sonarIssueScoring.service.prediction.load.Repo;

/** Base class for extracting the first / last part of a Filename */
public abstract class AbstractPrefixSuffixExtractor implements FeatureExtractor {
    protected static final String NONE = "<none>";
    protected static final CharMatcher uppercase = CharMatcher.inRange('A', 'Z');

    /**
     * Extracts the Prefix and Suffix of the filename
     * <p>
     * If a prefix / suffix is used by only one file, it is replaced with IsUnique instead.
     * If it is otherwise used less then 7 times, it is labeled as NonSpecial
     */

    @Override public void extractFeatures(Repo repo, Output out) {
        Map<Path, String> pathToExtracted = Maps.toMap(out.paths(), p -> extractRelevantPart(p.getFileName().toString()));
        ImmutableMultiset<String> histogram = ImmutableMultiset.copyOf(pathToExtracted.values());

        out.add(featureName(), path -> {
            String extracted = pathToExtracted.getOrDefault(path, "");
            int count = histogram.count(extracted);
            return count == 1 ? "IsUnique" : count < 7 ? "NonSpecial" : extracted;
        });
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
