package br.ufpe.cin.metric.extractor.features;

import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LineLengthFeatureTest {

    private final LineLengthFeature feature = new LineLengthFeature();

    @Test
    void mediaIgnoraLinhasEmBranco() {
        SourceFile file = new SourceFile("T.java", "aaaa\n\n   \nbbbbbb\n", null);

        FeatureResult result = feature.extract(file);

        assertEquals(LineLengthFeature.NAME, result.feature());
        assertEquals(5.0, result.mean(), 1e-9);
        assertEquals(result.mean(), result.max(), 1e-9);
    }

    @Test
    void fonteSemLinhasNaoVaziasRetornaZero() {
        FeatureResult result = feature.extract(new SourceFile("V.java", "\n   \n\n", null));

        assertEquals(0, result.mean());
    }
}
