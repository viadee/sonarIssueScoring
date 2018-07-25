package de.viadee.sonarIssueScoring.service.prediction.extract.sourcecode;

import org.springframework.stereotype.Service;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.ForeachStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

@Service
class CyclomaticComplexityCalculator {
    /** Calculates an approximation of the cyclomatic complexity according to https://stackoverflow.com/a/29047207 */
    public int cyclomaticComplexity(CompilationUnit cu) {
        return (int) cu.stream().filter(
                n -> n instanceof IfStmt || n instanceof ForStmt || n instanceof ForeachStmt || n instanceof WhileStmt || n instanceof DoStmt || //
                        n instanceof SwitchEntryStmt || n instanceof CatchClause || n instanceof ConditionalExpr || isBinaryOrOrAnd(n) || //
                        n instanceof MethodDeclaration || n instanceof InitializerDeclaration || n instanceof ReturnStmt) // Not in the above link, which is for single methods only
                .count();
    }

    private static boolean isBinaryOrOrAnd(Node n) {
        if (n instanceof BinaryExpr) {
            BinaryExpr bin = (BinaryExpr) n;
            return bin.getOperator() == Operator.OR || bin.getOperator() == Operator.AND;
        }
        return false;
    }
}
