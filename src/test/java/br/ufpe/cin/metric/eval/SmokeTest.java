package br.ufpe.cin.metric.eval;

import br.ufpe.cin.metric.eval.ControlRunner.ControlRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test da Seção 3.2: garante que código sabidamente bom recebe nota alta
 * (&gt; 7,0) e código sabidamente ruim recebe nota baixa (&lt; 4,0).
 *
 * Lê os exemplos sintéticos de controle de {@code eval/control} (caminho
 * relativo à raiz do projeto, que é o diretório de trabalho durante os testes).
 */
class SmokeTest {

    private static final Path CONTROL_DIR = Path.of("eval", "control");

    @TestFactory
    @DisplayName("Smoke test — ideal > 7,0 e problemático < 4,0")
    Stream<DynamicTest> controlSamplesFallInExpectedRange() {
        List<ControlRow> rows = ControlRunner.scoreControl(CONTROL_DIR);

        assertTrue(rows.size() >= 2, "deve haver exemplos de controle em " + CONTROL_DIR);

        return rows.stream().map(r -> DynamicTest.dynamicTest(
                String.format("%s (%s) = %.2f", r.sample(), r.category(), r.toolScore()),
                () -> assertTrue(r.pass(),
                        String.format("%s [%s] tirou %.2f, fora da faixa esperada (%s %.1f)",
                                r.sample(), r.category(), r.toolScore(),
                                r.category().equals(ControlRunner.IDEAL) ? ">" : "<", r.threshold()))
        ));
    }
}
