package de.viadee.sonarIssueScoring.misc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Style;

@Target({ElementType.PACKAGE, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@Style(typeAbstract = "Base*", typeImmutable = "*", allParameters = true, defaults = @Immutable(copy = false))
public @interface ImmutableStyle {}
