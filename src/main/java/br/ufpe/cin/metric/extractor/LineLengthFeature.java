package br.ufpe.cin.metric.extractor;

import java.util.List;

/**
 * Feature 2: Comprimento Médio de Linha.
 *
 * <p>Média de caracteres por linha não-vazia do arquivo, sobre o texto-fonte original.
 * É uma feature por-arquivo, então {@code max == mean}. Limiares (ver roteiro):
 * ≤ 80 excelente | 81-100 aceitável | &gt; 100 penalização progressiva.
 */
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
