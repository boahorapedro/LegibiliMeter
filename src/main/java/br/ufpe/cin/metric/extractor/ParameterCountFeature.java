package br.ufpe.cin.metric.extractor;

import com.github.javaparser.ast.body.MethodDeclaration;

import java.util.List;

/**
 * Feature 3: Número de Parâmetros de Método.
 *
 * <p>Conta os parâmetros formais de cada {@link MethodDeclaration} e agrega o
 * arquivo pelo pior método ({@code max}) e pela média entre métodos ({@code mean}).
 * Limiares (ver roteiro): ≤ 3 ideal | 4-5 aceitável | &gt; 5 problema de design.
 */
public class ParameterCountFeature implements Feature {

    public static final String NAME = "parameterCount";

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
            int params = method.getParameters().size();
            max = Math.max(max, params);
            sum += params;
        }

        double mean = (double) sum / methods.size();
        return new FeatureResult(NAME, max, mean);
    }
}
