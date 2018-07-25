package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.IntersectionType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.ast.type.TypeParameter;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.ast.type.WildcardType;

@Service
class TypeSimplifier {
    /**
     * Given type definition, resolve all contained simple names.
     * <p>
     * For instance, a List&lt;A extends Comparable & CharSequence&gt; would yield a stream of [List, Comparable, CharSequence]
     * <p>
     * Some types, such as lambda parameters or var statements are unresolvable, and not returned
     * Primitives and the void type are excluded as well.
     */
    public Stream<String> extractSimpleTypeNames(Type referencedType) {
        if (referencedType instanceof UnionType)
            return referencedType.asUnionType().getElements().stream().flatMap(this::extractSimpleTypeNames);

        if (referencedType instanceof IntersectionType)
            return referencedType.asIntersectionType().getElements().stream().flatMap(this::extractSimpleTypeNames);

        if (referencedType instanceof ClassOrInterfaceType)
            return Stream.of(referencedType.asClassOrInterfaceType().getNameAsString());

        if (referencedType instanceof ArrayType)
            return extractSimpleTypeNames(referencedType.asArrayType().getComponentType());

        if (referencedType instanceof TypeParameter)
            return referencedType.asTypeParameter().getTypeBound().stream().flatMap(this::extractSimpleTypeNames);

        if (referencedType instanceof WildcardType)
            return Stream.concat(referencedType.asWildcardType().getExtendedType().map(this::extractSimpleTypeNames).orElse(Stream.of()),
                    referencedType.asWildcardType().getSuperType().map(this::extractSimpleTypeNames).orElse(Stream.of()));

        //UnknownType, VarType, VoidType, PrimitiveType
        return Stream.empty();
    }
}
