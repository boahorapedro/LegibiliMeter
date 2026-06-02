package br.ufpe.cin.metric.extractor;

import br.ufpe.cin.metric.parser.JavaFileLoader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CognitiveComplexityFeatureTest {

    private final CognitiveComplexityFeature feature = new CognitiveComplexityFeature();

    @Test
    void exemploCanonicoSumOfPrimes() {
        // Exemplo do white paper de Campbell: CogC = 7
        // for(+1) > for(+2) > if(+3) + continue rotulado(+1)
        SourceFile file = SourceFile.of("P.java",
                "class P { int sumOfPrimes(int max) {"
                        + " int total = 0;"
                        + " OUTER: for (int i = 1; i <= max; ++i) {"
                        + "   for (int j = 2; j < i; ++j) {"
                        + "     if (i % j == 0) { continue OUTER; }"
                        + "   }"
                        + "   total += i;"
                        + " }"
                        + " return total; } }");

        assertEquals(7, feature.extract(file).max());
    }

    @Test
    void switchContaUmaVez() {
        SourceFile file = SourceFile.of("W.java",
                "class W { String w(int n) { switch (n) {"
                        + " case 1: return \"one\";"
                        + " case 2: return \"two\";"
                        + " default: return \"lots\"; } } }");

        assertEquals(1, feature.extract(file).max());
    }

    @Test
    void cadeiaElseIf() {
        // if(+1) + else if(+1) + else(+1) = 3
        SourceFile file = SourceFile.of("E.java",
                "class E { void f(int x) { if (x > 0) {} else if (x < 0) {} else {} } }");

        assertEquals(3, feature.extract(file).max());
    }

    @Test
    void sequenciasDeOperadoresLogicos() {
        // if(+1) + (a && b && c || d): && em sequência (+1) e alternância p/ || (+1) = 3
        SourceFile file = SourceFile.of("L.java",
                "class L { void f(boolean a, boolean b, boolean c, boolean d) {"
                        + " if (a && b && c || d) {} } }");

        assertEquals(3, feature.extract(file).max());
    }

    @Test
    void metodoLinearTemZero() {
        SourceFile file = SourceFile.of("Z.java",
                "class Z { int soma(int a, int b) { int r = a + b; return r; } }");

        assertEquals(0, feature.extract(file).max());
    }

    @Test
    void avaliaAmostraReal() throws Exception {
        // pior método: checkLowStock = for(1) + if aninhado(2) + && (1) = 4
        // soma dos 10 métodos = 13 → mean 1.3
        URL sample = getClass().getResource("/samples/InventoryManager.java");
        assertNotNull(sample, "amostra InventoryManager.java não encontrada no classpath");
        SourceFile file = JavaFileLoader.loadSource(new File(sample.toURI()).getPath());

        FeatureResult result = feature.extract(file);

        assertEquals(4, result.max());
        assertEquals(1.3, result.mean(), 1e-9);
    }
}
