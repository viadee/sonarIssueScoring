package de.viadee.sonarIssueScoring.service.prediction.extract.suffix;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import de.viadee.sonarIssueScoring.service.prediction.extract.FeatureExtractor;
import de.viadee.sonarIssueScoring.service.prediction.load.Repo;
import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;

import java.nio.file.Path;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

/** Base class for extracting the first / last part of a Filename */
public abstract class AbstractPrefixSuffixExtractor implements FeatureExtractor {
    protected static final String NONE = "<none>";
    protected static final CharMatcher uppercase = CharMatcher.inRange('A', 'Z');

    /**
     * Inserts the Prefix and Suffix of the filename into the output map.
     * <p>
     * If a prefix / suffix is used by only one file, it is replaced with IsUnique instead.
     * If it is otherwise used less then 7 times, it is labeled as NonSpecial
     */
    @Override public void extractFeatures(Repo repo, Map<Path, Builder> output) {
        ImmutableMap<Path, String> pathToSpecialMap = repo.snapshot().getAllFiles().keySet().stream().collect(
                toImmutableMap(p -> p, p -> extractRelevantPart(p.getFileName().toString())));

        ImmutableMultiset<String> histogram = ImmutableMultiset.copyOf(pathToSpecialMap.values());

        output.forEach((path, out) -> {
            String special = pathToSpecialMap.getOrDefault(path, "");
            int count = histogram.count(special);
            putResult(out, count == 1 ? "IsUnique" : count < 7 ? "NonSpecial" : special);
        });
    }

    protected abstract String extractRelevantPart(String filename);

    protected abstract void putResult(Builder out, String res);
}
