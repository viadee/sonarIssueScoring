package de.viadee.sonarIssueScoring.service.desirability;

import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PathKeyDeserializer extends KeyDeserializer {
    @Override public Path deserializeKey(String key, DeserializationContext ctxt) {
        return Paths.get(key);
    }
}
