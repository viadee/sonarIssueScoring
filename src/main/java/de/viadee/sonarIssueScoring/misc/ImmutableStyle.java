package de.viadee.sonarIssueScoring.misc;

import org.immutables.value.Value.Style;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Style(typeAbstract = "Base*", typeImmutable = "*", allParameters = true)
public @interface ImmutableStyle {}
