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

package de.viadee.sonarIssueScoring.service.prediction.extract.source;

import org.eclipse.jdt.core.dom.*;

import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import com.github.mauricioaniche.ck.metric.ClassInfo;
import com.github.mauricioaniche.ck.metric.Metric;

/**
 * This is mostly a copy of CBO.java from the CK library, which is licensed under Apache2
 */
class DependencyVisitor extends ASTVisitor implements Metric {
    private final DependencyGraph graph;
    private String className;

    public DependencyVisitor(DependencyGraph graph) {
        this.graph = graph;
    }

    @Override public boolean visit(VariableDeclarationStatement node) {
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override public boolean visit(ClassInstanceCreation node) {
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override public boolean visit(ArrayCreation node) {
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override public boolean visit(FieldDeclaration node) {
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override public boolean visit(ReturnStatement node) {
        if (node.getExpression() != null)
            coupleTo(node.getExpression().resolveTypeBinding());
        return super.visit(node);
    }

    @Override public boolean visit(TypeLiteral node) {
        coupleTo(node.resolveTypeBinding());
        coupleTo(node.getType().resolveBinding());
        return super.visit(node);
    }

    @Override public boolean visit(ThrowStatement node) {
        coupleTo(node.getExpression().resolveTypeBinding());
        return super.visit(node);
    }

    @Override public boolean visit(TypeDeclaration node) {
        ITypeBinding type = node.resolveBinding();

        if (type != null) {
            ITypeBinding binding = type.getSuperclass();
            if (binding != null)
                coupleTo(binding);

            for (ITypeBinding interfaces : type.getInterfaces())
                coupleTo(interfaces);
        }

        return super.visit(node);
    }

    @Override public boolean visit(MethodDeclaration node) {

        IMethodBinding method = node.resolveBinding();
        if (method == null)
            return super.visit(node);

        coupleTo(method.getReturnType());

        for (ITypeBinding param : method.getParameterTypes())
            coupleTo(param);

        return super.visit(node);
    }

    @Override public boolean visit(CastExpression node) {
        coupleTo(node.getType().resolveBinding());

        return super.visit(node);
    }

    @Override public boolean visit(InstanceofExpression node) {

        coupleTo(node.getRightOperand().resolveBinding());
        coupleTo(node.getLeftOperand().resolveTypeBinding());

        return super.visit(node);
    }

    @Override public boolean visit(NormalAnnotation node) {
        coupleTo(node.resolveTypeBinding());
        return super.visit(node);
    }

    @Override public boolean visit(MarkerAnnotation node) {
        coupleTo(node.resolveTypeBinding());
        return super.visit(node);
    }

    @Override public boolean visit(SingleMemberAnnotation node) {
        coupleTo(node.resolveTypeBinding());
        return super.visit(node);
    }

    @Override public boolean visit(ParameterizedType node) {
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

        graph.addDependency(className, resolved.getErasure().getQualifiedName());

        for (int i = 0; i < resolved.getTypeArguments().length; i++) {
            if (resolved.getTypeArguments()[i].isWildcardType() && resolved.getTypeArguments()[i].getBound() != null)
                graph.addDependency(className, clean(resolved.getTypeArguments()[i].getBound().getErasure().getQualifiedName()));
            graph.addDependency(className, clean(resolved.getTypeArguments()[i].getErasure().getQualifiedName())); //getErasure is not always returning a raw type
        }
    }

    private static String clean(String in) {
        int indexOpen = in.indexOf('<');
        if (indexOpen >= 0)
            return in.substring(0, indexOpen);
        return in;
    }

    @Override public void execute(CompilationUnit cu, CKNumber result, CKReport report) {
        ClassInfo classInfo = new ClassInfo();
        cu.accept(classInfo);
        className = classInfo.getClassName() == null ? "" : classInfo.getClassName();

        cu.accept(this);
    }

    @Override public void setResult(CKNumber result) {
        //Nothing to do
    }
}
