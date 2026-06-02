package br.ufpe.cin.metric.extractor;

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

import java.util.List;

/**
 * Feature 5: Complexidade Cognitiva (Campbell, 2018).
 *
 * <p>Para cada método, pontua o esforço de leitura do fluxo de controle: +1 por
 * estrutura ({@code if}, {@code for}, {@code while}, {@code do}, {@code switch},
 * {@code catch}, ternário), mais uma penalidade igual ao nível de aninhamento atual.
 * {@code else}/{@code else if} somam +1 sem penalidade de aninhamento; sequências de
 * operadores lógicos ({@code &&}/{@code ||}) somam +1 por sequência; {@code break}/
 * {@code continue} rotulados somam +1. Lambdas aprofundam o aninhamento sem somar base.
 *
 * <p>Fora de escopo nesta versão: bônus para lambdas simples e +1 por recursão
 * (exige resolução de nomes). Agrega o arquivo por pior método ({@code max}) e média.
 * Limiares (ver roteiro): 0-5 excelente | 6-10 boa | 11-15 aceitável | ≥ 25 crítica.
 */
public class CognitiveComplexityFeature implements Feature {

    public static final String NAME = "cognitiveComplexity";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FeatureResult extract(SourceFile file) {
        List<MethodDeclaration> methods = file.ast().findAll(MethodDeclaration.class);
        if (methods.isEmpty()) {
            return new FeatureResult(NAME, 0, 0);
        }

        int max = 0;
        long sum = 0;
        for (MethodDeclaration method : methods) {
            int complexity = complexityOf(method);
            max = Math.max(max, complexity);
            sum += complexity;
        }

        double mean = (double) sum / methods.size();
        return new FeatureResult(NAME, max, mean);
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
