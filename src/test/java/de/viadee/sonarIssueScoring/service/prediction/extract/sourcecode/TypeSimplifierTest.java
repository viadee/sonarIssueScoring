package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.PrimitiveType.Primitive;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.UnknownType;
import com.github.javaparser.ast.type.VarType;
import com.github.javaparser.ast.type.VoidType;
import com.github.javaparser.ast.type.WildcardType;
import com.google.common.collect.ImmutableSet;

public class TypeSimplifierTest {
    @Test
    public void testUnionType() {
        Type type = new UnionType(
                new NodeList<>(JavaParser.parseClassOrInterfaceType("some.IOException"), JavaParser.parseClassOrInterfaceType("a.NullPointerException")));

        assertStream(type, "IOException", "NullPointerException");
    }

    @Test
    public void testIntersection() {
        Type type = new IntersectionType(
                new NodeList<>(JavaParser.parseClassOrInterfaceType("some.IOException"), JavaParser.parseClassOrInterfaceType("a.NullPointerException")));

        assertStream(type, "IOException", "NullPointerException");
    }

    @Test
    public void testClassOrInterface() {
        Type type = JavaParser.parseClassOrInterfaceType("ide.is.Great");
        assertStream(type, "Great");
    }

    @Test
    public void testArray() {
        //ide.is.Great[][]
        Type type = new ArrayType(new ArrayType(JavaParser.parseClassOrInterfaceType("ide.is.Great")));
        assertStream(type, "Great");
    }

    @Test
    public void testTypeParameter() {
        Type type = new TypeParameter("T", new NodeList<>(JavaParser.parseClassOrInterfaceType("ide.is.Great")));
        assertStream(type, "Great");
    }

    @Test
    public void testWildcard() {
        Type type = new WildcardType(JavaParser.parseClassOrInterfaceType("ide.is.Great"), JavaParser.parseClassOrInterfaceType("sata.is.New"), new NodeList<>());
        assertStream(type, "Great", "New");
    }

    @Test
    public void testIgnored() {
        assertStream(new VoidType());
        assertStream(new VarType());
        assertStream(new PrimitiveType(Primitive.BYTE));
        assertStream(new UnknownType());
    }

    private static void assertStream(Type type, String... elements) { //Argument order required by varargs
        Set<String> actual = new TypeSimplifier().extractSimpleTypeNames(type).collect(Collectors.toSet());

        Assert.assertEquals(ImmutableSet.copyOf(elements), actual);
    }
}