package br.ufpe.cin.metric.extractor;

import br.ufpe.cin.metric.parser.JavaFileLoader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ParameterCountFeatureTest {

    private final ParameterCountFeature feature = new ParameterCountFeature();

    @Test
    void contaMaxEMediaComFonteControlada() {
        // params: 0, 1, 3
        SourceFile file = SourceFile.of("T.java",
                "class T { void a() {} void b(int x) {} void c(int x, int y, int z) {} }");

        FeatureResult result = feature.extract(file);

        assertEquals(ParameterCountFeature.NAME, result.feature());
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
        // 10 métodos; addProduct tem 4 params (max); soma=10 → mean=1.0
        URL sample = getClass().getResource("/samples/InventoryManager.java");
        assertNotNull(sample, "amostra InventoryManager.java não encontrada no classpath");
        SourceFile file = JavaFileLoader.loadSource(new File(sample.toURI()).getPath());

        FeatureResult result = feature.extract(file);

        assertEquals(4, result.max());
        assertEquals(1.0, result.mean(), 1e-9);
    }
}
