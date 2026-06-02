package br.ufpe.cin.metric.extractor;

import br.ufpe.cin.metric.parser.JavaFileLoader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NestingDepthFeatureTest {

    private final NestingDepthFeature feature = new NestingDepthFeature();

    @Test
    void medeAninhamentoPorMetodo() {
        // profundidades: 0, 1, 3 (for→while→if)
        SourceFile file = SourceFile.of("T.java",
                "class T {"
                        + " void flat() { int x = 1; }"
                        + " void one() { if (x) {} }"
                        + " void three() { for (;;) { while (b) { if (c) {} } } }"
                        + "}");

        FeatureResult result = feature.extract(file);

        assertEquals(NestingDepthFeature.NAME, result.feature());
        assertEquals(3, result.max());
        assertEquals(4.0 / 3.0, result.mean(), 1e-9);
    }

    @Test
    void arquivoSemMetodosRetornaZero() {
        FeatureResult result = feature.extract(SourceFile.of("V.java", "class Vazia { int campo = 1; }"));

        assertEquals(0, result.max());
        assertEquals(0, result.mean());
    }

    @Test
    void avaliaAmostraReal() throws Exception {
        // pior método (checkLowStock / listProducts) aninha for→if = 2; soma=9 em 10 métodos
        URL sample = getClass().getResource("/samples/InventoryManager.java");
        assertNotNull(sample, "amostra InventoryManager.java não encontrada no classpath");
        SourceFile file = JavaFileLoader.loadSource(new File(sample.toURI()).getPath());

        FeatureResult result = feature.extract(file);

        assertEquals(2, result.max());
        assertEquals(0.9, result.mean(), 1e-9);
    }
}
