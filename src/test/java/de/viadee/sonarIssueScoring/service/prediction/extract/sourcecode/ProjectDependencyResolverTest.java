package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.LocalClassDeclarationStmt;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

public class ProjectDependencyResolverTest {

    @Test
    public void testSimpleTypeExtraction() {
        String source = "package pkg;";
        source += "import pkg2.System;";
        source += "class Clazz extends Name1 {"; //Clazz, Name1
        source += "  public String method() throws NPE {"; //String, NPE
        source += "      System.out.println(Runtime.getRuntime());"; // NOT FOUND: System, Runtime
        source += "  }";
        source += "  public <T extends CharSequence> Map.Entry inner(){"; //CharSequence, Entry
        source += "      try{}catch(AOE|BOE e){}"; //AOE, BOE
        source += "  }}";

        Assert.assertEquals(ImmutableSet.of("Name1", "String", "NPE", /*"System", "Runtime",*/"CharSequence", "Map", "Entry", "AOE", "BOE"),
                ProjectDependencyResolver.extractReferencedSimpleTypes(JavaParser.parse(source)));
    }

    @Test
    public void testImports() {
        CompilationUnit cu = new CompilationUnit("one.two");
        cu.addImport("one.two.Three", false, false);
        cu.addImport("one.three.Four.four", true, false);
        cu.addImport("one.three.Five", true, true);
        cu.addImport("one.four", false, true);
        cu.addImport("one.five.Six", false, false);

        Assert.assertEquals(ImmutableSet.of("", "one.two", "one.four"), ProjectDependencyResolver.extractWildcardImports(cu));
        Assert.assertEquals(ImmutableMap.of(//
                "Three", "one.two.Three",//
                "Six", "one.five.Six"), ProjectDependencyResolver.extractFQImports(cu));
    }

    @Test
    public void testFullyQualifiedName() {
        CompilationUnit cu = new CompilationUnit("one.two");
        ClassOrInterfaceDeclaration c1 = cu.addClass("Three");
        ClassOrInterfaceDeclaration c2 = new ClassOrInterfaceDeclaration(EnumSet.noneOf(Modifier.class), false, "Four");
        c1.addMember(c2);

        Assert.assertEquals(Optional.of("one.two.Three.Four"), ProjectDependencyResolver.buildFullyQualifiedName(c2));


        MethodDeclaration method = c2.addMethod("m");
        ClassOrInterfaceDeclaration inner = new ClassOrInterfaceDeclaration(EnumSet.noneOf(Modifier.class), false, "Five");
        method.createBody().addStatement(new LocalClassDeclarationStmt(inner));

        Assert.assertEquals(Optional.empty(), ProjectDependencyResolver.buildFullyQualifiedName(inner));
    }

    @Test
    public void testSimpleName() {
        Assert.assertEquals(ImmutableSet.of("IOException", "NullPointerException"), ProjectDependencyResolver.extractSimpleTypes(
                new UnionType(new NodeList<>(JavaParser.parseClassOrInterfaceType("some.IOException"), JavaParser.parseClassOrInterfaceType("a.NullPointerException")))));

        Assert.assertEquals(ImmutableSet.of("IOException", "NullPointerException"), ProjectDependencyResolver.extractSimpleTypes(new IntersectionType(
                new NodeList<>(JavaParser.parseClassOrInterfaceType("some.IOException"), JavaParser.parseClassOrInterfaceType("a.NullPointerException")))));

        Assert.assertEquals(ImmutableSet.of("Great"), ProjectDependencyResolver.extractSimpleTypes(JavaParser.parseClassOrInterfaceType("ide.is.Great")));

        //ide.is.Great[][]
        Assert.assertEquals(ImmutableSet.of("Great"),
                ProjectDependencyResolver.extractSimpleTypes(new ArrayType(new ArrayType(JavaParser.parseClassOrInterfaceType("ide.is.Great")))));

        Assert.assertEquals(ImmutableSet.of("Great"),
                ProjectDependencyResolver.extractSimpleTypes(new TypeParameter("T", new NodeList<>(JavaParser.parseClassOrInterfaceType("ide.is.Great")))));

        Assert.assertEquals(ImmutableSet.of("Great", "New"), ProjectDependencyResolver.extractSimpleTypes(
                new WildcardType(JavaParser.parseClassOrInterfaceType("ide.is.Great"), JavaParser.parseClassOrInterfaceType("sata.is.New"), new NodeList<>())));

        Assert.assertEquals(ImmutableSet.of(), ProjectDependencyResolver.extractSimpleTypes(new VoidType()));
        Assert.assertEquals(ImmutableSet.of(), ProjectDependencyResolver.extractSimpleTypes(new VarType()));
        Assert.assertEquals(ImmutableSet.of(), ProjectDependencyResolver.extractSimpleTypes(new PrimitiveType(Primitive.BYTE)));
        Assert.assertEquals(ImmutableSet.of(), ProjectDependencyResolver.extractSimpleTypes(new UnknownType()));
    }
}