package br.ufpe.cin.metric.extractor;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

import java.util.List;

/**
 * Feature 1: Profundidade de Aninhamento.
 *
 * <p>Para cada método, mede o nível máximo de blocos de controle encadeados
 * ({@code if}, {@code for}, {@code while}, {@code do}, {@code try}, {@code switch}).
 * Agrega o arquivo pelo pior método ({@code max}) e pela média entre métodos ({@code mean}).
 * Limiares (ver roteiro): ≤ 2 excelente | 3 aceitável | 4 problemático | ≥ 5 crítico.
 */
public class NestingDepthFeature implements Feature {

    public static final String NAME = "nestingDepth";

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
            int depth = maxControlNesting(method);
            max = Math.max(max, depth);
            sum += depth;
        }

        double mean = (double) sum / methods.size();
        return new FeatureResult(NAME, max, mean);
    }

    private int maxControlNesting(Node node) {
        int best = 0;
        for (Node child : node.getChildNodes()) {
            int childDepth = maxControlNesting(child);
            if (isControlStructure(child)) {
                childDepth += 1;
            }
            best = Math.max(best, childDepth);
        }
        return best;
    }

    private boolean isControlStructure(Node node) {
        return node instanceof IfStmt
                || node instanceof ForStmt
                || node instanceof ForEachStmt
                || node instanceof WhileStmt
                || node instanceof DoStmt
                || node instanceof TryStmt
                || node instanceof SwitchStmt;
    }
}
