package de.viadee.sonarissuescoring.web;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

import de.viadee.sonarissuescoring.service.desirability.DesirabilityResult;
import de.viadee.sonarissuescoring.service.desirability.IssueDesirability;

@Component
public class DesirabilityMapMessageConverterPlaintext extends ToStringMessageConverter<DesirabilityResult> {

    public DesirabilityMapMessageConverterPlaintext() {
        super(DesirabilityResult.class);
    }

    @Override protected String write(DesirabilityResult src) {
        Map<String, Double> scoreMap = Maps.transformValues(src.desirabilities(), IssueDesirability::desirabilityScore);

        return StringTableFormatter.formatData("Issue Desirability", "Issue", "Desirability Score", scoreMap, true);
    }
}
