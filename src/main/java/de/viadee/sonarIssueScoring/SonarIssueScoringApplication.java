package de.viadee.sonarIssueScoring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

@SpringBootApplication
@Configuration
public class SonarIssueScoringApplication {
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer configureObjectMapper() {
        return builder -> builder.modules(new GuavaModule(), new Jdk8Module()).failOnUnknownProperties(true);
    }

    public static void main(String[] args) {
        SpringApplication.run(SonarIssueScoringApplication.class, args);
    }
}