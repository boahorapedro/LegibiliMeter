package br.ufpe.cin.metric.extractor;

import br.ufpe.cin.metric.extractor.features.CognitiveComplexityFeature;
import br.ufpe.cin.metric.extractor.features.IdentifierLengthFeature;
import br.ufpe.cin.metric.extractor.features.LineLengthFeature;
import br.ufpe.cin.metric.extractor.features.NestingDepthFeature;
import br.ufpe.cin.metric.extractor.features.ParameterCountFeature;
import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Orquestrador da camada de extração: roda todas as {@link Feature} sobre um
 * {@link SourceFile} e devolve o mapa {@code nome → valor bruto} consumido pelo
 * {@link br.ufpe.cin.metric.scorer.Scorer}.
 *
 * Adicionar uma nova feature à métrica = incluí-la em {@link #defaultFeatures()}.
 */
public class FeatureExtractor {

    private final List<Feature> features;

    public FeatureExtractor() {
        this(defaultFeatures());
    }

    public FeatureExtractor(List<Feature> features) {
        this.features = features;
    }

    /** As cinco features que compõem a métrica de legibilidade. */
    public static List<Feature> defaultFeatures() {
        return List.of(
                new CognitiveComplexityFeature(),
                new NestingDepthFeature(),
                new IdentifierLengthFeature(),
                new ParameterCountFeature(),
                new LineLengthFeature()
        );
    }

    /**
     * Executa cada feature sobre o arquivo e preserva a ordem de inserção,
     * de modo que o relatório sempre lista as features na mesma sequência.
     */
    public Map<String, FeatureResult> extractAll(SourceFile file) {
        Map<String, FeatureResult> results = new LinkedHashMap<>();
        for (Feature feature : features) {
            results.put(feature.name(), feature.extract(file));
        }
        return results;
    }
}
