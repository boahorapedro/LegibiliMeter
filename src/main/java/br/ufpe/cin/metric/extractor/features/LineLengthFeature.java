package br.ufpe.cin.metric.extractor.features;

import br.ufpe.cin.metric.extractor.Feature;
import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;

import java.util.List;

public class LineLengthFeature implements Feature {

    public static final String NAME = "lineLength";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public FeatureResult extract(SourceFile file) {
        List<String> lines = file.nonBlankLines();
        if (lines.isEmpty()) {
            return FeatureResult.single(NAME, 0);
        }

        double average = lines.stream()
                .mapToInt(String::length)
                .average()
                .orElse(0);
        return FeatureResult.single(NAME, average);
    }
}
