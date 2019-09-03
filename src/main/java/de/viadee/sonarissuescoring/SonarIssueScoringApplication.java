package de.viadee.sonarissuescoring;

import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

@SpringBootApplication
@Configuration
public class SonarIssueScoringApplication {
    @Bean public Jackson2ObjectMapperBuilderCustomizer configureObjectMapper() {
        return builder -> builder.modules(new GuavaModule(), new Jdk8Module(), new ParameterNamesModule()).failOnUnknownProperties(true);
    }

    public static void main(String[] args) {
        boolean runEvaluation = Evaluator.willRunEvaluation(new DefaultApplicationArguments(args));
        new SpringApplicationBuilder().
                main(SonarIssueScoringApplication.class).
                sources(SonarIssueScoringApplication.class).
                web(runEvaluation ? WebApplicationType.NONE : WebApplicationType.SERVLET).
                run(args);
    }
}
