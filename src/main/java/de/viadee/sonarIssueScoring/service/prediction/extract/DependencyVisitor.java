/*
 * Copyright 2018 Maurício Aniche
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.viadee.sonarIssueScoring.service.prediction.extract;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import com.github.mauricioaniche.ck.metric.Metric;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;

/**
 * This is mostly a copy of CBO.java from the CK library, which is licensed under Apache2
 */
class DependencyVisitor extends ASTVisitor implements Metric {
    private final Set<String> dependencies = new HashSet<>();
    private final SetMultimap<String, String> references;
    private final Multiset<String> comments;

    public DependencyVisitor(SetMultimap<String, String> references, Multiset<String> comments) {
        this.references = references;
        this.comments = comments;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(ClassInstanceCreation node) {
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(ArrayCreation node) {
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(ReturnStatement node) {
        if (node.getExpression() != null)
            coupleTo(node.getExpression().resolveTypeBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(TypeLiteral node) {
        coupleTo(node.resolveTypeBinding());
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(ThrowStatement node) {
        coupleTo(node.getExpression().resolveTypeBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        ITypeBinding type = node.resolveBinding();

        ITypeBinding binding = type.getSuperclass();
        if (binding != null)
            coupleTo(binding);

        for (ITypeBinding interfaces : type.getInterfaces())
            coupleTo(interfaces);

        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {

        IMethodBinding method = node.resolveBinding();
        if (method == null)
            return super.visit(node);

        coupleTo(method.getReturnType());

        for (ITypeBinding param : method.getParameterTypes())
            coupleTo(param);

        return super.visit(node);
    }

    @Override
    public boolean visit(CastExpression node) {
        coupleTo(node.getType().resolveBinding());

        return super.visit(node);
    }

    @Override
    public boolean visit(InstanceofExpression node) {

        coupleTo(node.getRightOperand().resolveBinding());
        coupleTo(node.getLeftOperand().resolveTypeBinding());

        return super.visit(node);
    }

    @Override
    public boolean visit(NormalAnnotation node) {
        coupleTo(node.resolveTypeBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(MarkerAnnotation node) {
        coupleTo(node.resolveTypeBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(SingleMemberAnnotation node) {
        coupleTo(node.resolveTypeBinding());
        return super.visit(node);
    }

    @Override
    public boolean visit(ParameterizedType node) {
        ITypeBinding binding = node.resolveBinding();
        if (binding == null)
            return super.visit(node);

        coupleTo(binding);

        for (ITypeBinding types : binding.getTypeArguments())
            coupleTo(types);

        return super.visit(node);
    }

    private void coupleTo(ITypeBinding binding) {
        if (binding == null || binding.isNullType() || binding.isPrimitive())
            return;
        if (binding.isWildcardType())
            return;

        ITypeBinding resolved = binding;
        while (resolved.isArray())
            resolved = resolved.getComponentType();

        dependencies.add(resolved.getErasure().getQualifiedName());

        for (int i = 0; i < resolved.getTypeArguments().length; i++) {
            if (resolved.getTypeArguments()[i].isWildcardType() && resolved.getTypeArguments()[i].getBound() != null)
                dependencies.add(clean(resolved.getTypeArguments()[i].getBound().getErasure().getQualifiedName()));
            dependencies.add(clean(resolved.getTypeArguments()[i].getErasure().getQualifiedName())); //getErasure is not always returning a raw type
        }
    }

    private static String clean(String in) {
        int indexOpen = in.indexOf('<');
        if (indexOpen >= 0)
            return in.substring(0, indexOpen);
        return in;
    }

    @Override
    public void execute(CompilationUnit cu, CKNumber number, CKReport report) {
        cu.accept(this);
        references.putAll(number.getClassName(), dependencies);

        comments.add(number.getFile(), cu.getCommentList().size());
    }

    @Override
    public void setResult(CKNumber result) {}
}
