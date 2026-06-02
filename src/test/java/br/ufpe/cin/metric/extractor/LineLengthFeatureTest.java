package br.ufpe.cin.metric.extractor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineLengthFeatureTest {

    private final LineLengthFeature feature = new LineLengthFeature();

    @Test
    void mediaIgnoraLinhasEmBranco() {
        // linhas não-vazias de 4 e 6 chars → média 5; max == mean (feature por-arquivo)
        // ast irrelevante para esta feature; só o texto importa
        SourceFile file = new SourceFile("T.java", "aaaa\n\n   \nbbbbbb\n", null);

        FeatureResult result = feature.extract(file);

        assertEquals(LineLengthFeature.NAME, result.feature());
        assertEquals(5.0, result.mean(), 1e-9);
        assertEquals(result.mean(), result.max(), 1e-9);
    }

    @Test
    void fonteSemLinhasNaoVaziasRetornaZero() {
        // ast irrelevante para esta feature; só o texto importa
        FeatureResult result = feature.extract(new SourceFile("V.java", "\n   \n\n", null));

        assertEquals(0, result.mean());
    }
}
