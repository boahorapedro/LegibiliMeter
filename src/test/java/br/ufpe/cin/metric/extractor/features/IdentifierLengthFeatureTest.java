package br.ufpe.cin.metric.extractor.features;

import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;

import br.ufpe.cin.metric.parser.JavaFileLoader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IdentifierLengthFeatureTest {

    private final IdentifierLengthFeature feature = new IdentifierLengthFeature();

    @Test
    void mediaSobreNomesDeclarados() {
        SourceFile file = SourceFile.of("Abc.java",
                "class Abc { int xy; void run(int p) { int value = 1; } }");

        FeatureResult result = feature.extract(file);

        assertEquals(IdentifierLengthFeature.NAME, result.feature());
        assertEquals(2.8, result.mean(), 1e-9);
        assertEquals(result.mean(), result.max(), 1e-9);
    }

    @Test
    void avaliaAmostraReal() throws Exception {
        URL sample = getClass().getResource("/samples/InventoryManager.java");
        assertNotNull(sample, "amostra InventoryManager.java não encontrada no classpath");
        SourceFile file = JavaFileLoader.loadSource(new File(sample.toURI()).getPath());

        FeatureResult result = feature.extract(file);

        assertTrue(result.mean() > 0 && result.mean() < 8,
                "esperava média de identificadores curta, foi " + result.mean());
    }
}
