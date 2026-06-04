package br.ufpe.cin.metric.extractor.features;

import br.ufpe.cin.metric.extractor.Feature;
import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.util.ArrayList;
import java.util.List;

public class IdentifierLengthFeature implements Feature {

    public static final String NAME = "identifierLength";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FeatureResult extract(SourceFile file) {
        List<String> names = new ArrayList<>();
        file.ast().findAll(TypeDeclaration.class).forEach(t -> names.add(t.getNameAsString()));
        file.ast().findAll(MethodDeclaration.class).forEach(m -> names.add(m.getNameAsString()));
        file.ast().findAll(Parameter.class).forEach(p -> names.add(p.getNameAsString()));
        file.ast().findAll(VariableDeclarator.class).forEach(v -> names.add(v.getNameAsString()));

        if (names.isEmpty()) {
            return FeatureResult.single(NAME, 0);
        }

        double average = names.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0);
        return FeatureResult.single(NAME, average);
    }
}
