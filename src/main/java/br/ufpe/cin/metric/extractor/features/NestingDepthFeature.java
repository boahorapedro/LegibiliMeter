package br.ufpe.cin.metric.extractor.features;

import br.ufpe.cin.metric.extractor.MethodLevelFeature;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.stmt.WhileStmt;

public class NestingDepthFeature extends MethodLevelFeature {

    public static final String NAME = "nestingDepth";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected int measure(MethodDeclaration method) {
        return maxControlNesting(method);
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
