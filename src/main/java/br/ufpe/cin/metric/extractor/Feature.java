package br.ufpe.cin.metric.extractor;

import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;

public interface Feature {

    String name();

    FeatureResult extract(SourceFile file);
}
