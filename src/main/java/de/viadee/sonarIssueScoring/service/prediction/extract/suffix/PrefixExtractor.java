package de.viadee.sonarIssueScoring.service.prediction.extract.suffix;

import de.viadee.sonarIssueScoring.service.prediction.train.Instance.Builder;
import org.springframework.stereotype.Component;

@Component
public class PrefixExtractor extends AbstractPrefixSuffixExtractor {
    @Override protected String extractRelevantPart(String filename) {
        int i = uppercase.indexIn(filename, 1);
        return i == -1 ? NONE : filename.substring(0, i);
    }

    @Override protected void putResult(Builder out, String res) {
        out.prefix(res);
    }
}
