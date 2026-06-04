package br.ufpe.cin.metric.extractor.features;

import br.ufpe.cin.metric.extractor.MethodLevelFeature;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

public class CognitiveComplexityFeature extends MethodLevelFeature {

    public static final String NAME = "cognitiveComplexity";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected int measure(MethodDeclaration method) {
        return complexityOf(method);
    }

    private int complexityOf(MethodDeclaration method) {
        int[] acc = {0};
        method.getBody().ifPresent(body -> scoreNode(body, 0, acc));
        return acc[0];
    }

    private void scoreNode(Node node, int nesting, int[] acc) {
        if (node instanceof IfStmt ifStmt) {
            scoreIf(ifStmt, nesting, acc);
            return;
        }
        if (node instanceof ForStmt || node instanceof ForEachStmt
                || node instanceof WhileStmt || node instanceof DoStmt
                || node instanceof SwitchStmt || node instanceof CatchClause
                || node instanceof ConditionalExpr) {
            acc[0] += 1 + nesting;
            scoreChildren(node, nesting + 1, acc);
            return;
        }
        if (node instanceof BreakStmt brk && brk.getLabel().isPresent()) {
            acc[0] += 1;
            return;
        }
        if (node instanceof ContinueStmt cont && cont.getLabel().isPresent()) {
            acc[0] += 1;
            return;
        }
        if (node instanceof BinaryExpr bin && isLogical(bin)) {
            acc[0] += logicalSequences(bin, null);
            scoreLogicalOperands(bin, nesting, acc);
            return;
        }
        if (node instanceof LambdaExpr) {
            scoreChildren(node, nesting + 1, acc);
            return;
        }
        scoreChildren(node, nesting, acc);
    }

    private void scoreIf(IfStmt ifStmt, int nesting, int[] acc) {
        acc[0] += 1 + nesting;
        scoreNode(ifStmt.getCondition(), nesting, acc);
        scoreNode(ifStmt.getThenStmt(), nesting + 1, acc);
        ifStmt.getElseStmt().ifPresent(elseStmt -> scoreElse(elseStmt, nesting, acc));
    }

    private void scoreElse(Statement elseStmt, int nesting, int[] acc) {
        if (elseStmt instanceof IfStmt elseIf) {
            acc[0] += 1;
            scoreNode(elseIf.getCondition(), nesting, acc);
            scoreNode(elseIf.getThenStmt(), nesting + 1, acc);
            elseIf.getElseStmt().ifPresent(next -> scoreElse(next, nesting, acc));
        } else {
            acc[0] += 1;
            scoreNode(elseStmt, nesting + 1, acc);
        }
    }

    private void scoreChildren(Node node, int nesting, int[] acc) {
        for (Node child : node.getChildNodes()) {
            scoreNode(child, nesting, acc);
        }
    }

    private int logicalSequences(Expression expr, BinaryExpr.Operator parentOp) {
        if (expr instanceof BinaryExpr bin && isLogical(bin)) {
            int count = bin.getOperator() != parentOp ? 1 : 0;
            count += logicalSequences(bin.getLeft(), bin.getOperator());
            count += logicalSequences(bin.getRight(), bin.getOperator());
            return count;
        }
        return 0;
    }

    private void scoreLogicalOperands(Expression expr, int nesting, int[] acc) {
        if (expr instanceof BinaryExpr bin && isLogical(bin)) {
            scoreLogicalOperands(bin.getLeft(), nesting, acc);
            scoreLogicalOperands(bin.getRight(), nesting, acc);
        } else {
            scoreNode(expr, nesting, acc);
        }
    }

    private boolean isLogical(BinaryExpr bin) {
        return bin.getOperator() == BinaryExpr.Operator.AND
                || bin.getOperator() == BinaryExpr.Operator.OR;
    }
}
