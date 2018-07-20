package de.viadee.sonarIssueScoring.service.prediction.extract.suffix;

import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;
import org.springframework.stereotype.Component;

@Component
public class SuffixExtractor extends AbstractPrefixSuffixExtractor {
    @Override protected String extractRelevantPart(String filename) {
        int i = uppercase.lastIndexIn(filename);
        return i == -1 ? NONE : filename.substring(i, filename.length() - 5);
    }

    @Override protected void putResult(Builder out, String res) {
        out.suffix(res);
    }
}
