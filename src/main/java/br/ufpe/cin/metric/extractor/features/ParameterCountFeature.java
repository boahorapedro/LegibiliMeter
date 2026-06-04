package br.ufpe.cin.metric.extractor.features;

import br.ufpe.cin.metric.extractor.MethodLevelFeature;

import com.github.javaparser.ast.body.MethodDeclaration;

public class ParameterCountFeature extends MethodLevelFeature {

    public static final String NAME = "parameterCount";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    protected int measure(MethodDeclaration method) {
        return method.getParameters().size();
    }
}
