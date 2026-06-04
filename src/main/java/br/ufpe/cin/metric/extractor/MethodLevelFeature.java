package br.ufpe.cin.metric.extractor;

import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.List;

public abstract class MethodLevelFeature implements Feature {

    protected abstract int measure(MethodDeclaration method);

    @Override
    public FeatureResult extract(SourceFile file) {
        List<MethodDeclaration> methods = file.ast().findAll(MethodDeclaration.class);
        if (methods.isEmpty()) {
            return new FeatureResult(name(), 0, 0);
        }

        int max = 0;
        long sum = 0;
        for (MethodDeclaration method : methods) {
            int value = measure(method);
            max = Math.max(max, value);
            sum += value;
        }

        double mean = (double) sum / methods.size();
        return new FeatureResult(name(), max, mean);
    }
}
