package br.ufpe.cin.metric.scorer;

import br.ufpe.cin.metric.extractor.features.*;
import br.ufpe.cin.metric.model.FeatureResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários do Scorer.
 *
 * Os valores esperados são calculados diretamente pela fórmula do grupo:
 *   score = max(0, 1 - valor / limiar)
 *
 * A nota final é:
 *   (soma ponderada dos scores [0-1]) * 10
 */
class ScorerTest {

    private Scorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new Scorer();
    }

    // =========================================================================
    // Helper: fórmula de referência para calcular valores esperados nos testes
    // =========================================================================
    private static double esperado(double valor, double limiar) {
        return Math.max(0.0, 1.0 - valor / limiar);
    }

    private static FeatureResult result(String name, double value) {
        return FeatureResult.single(name, value);
    }

    // =========================================================================
    // Complexidade Cognitiva  (limiar = 15)
    // =========================================================================
    @Nested
    @DisplayName("CognitiveComplexity — score = max(0, 1 - CogC / 15)")
    class CognitiveComplexityTests {

        @Test @DisplayName("CogC = 0  → score 1.00")
        void cogc_0() {
            var fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 0));
            assertEquals(1.0, fs.score(), 0.001);
        }

        @Test @DisplayName("CogC = 3  → score 0.80")
        void cogc_3() {
            var fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 3));
            assertEquals(esperado(3, 15), fs.score(), 0.01);
        }

        @Test @DisplayName("CogC = 5  → score 0.67")
        void cogc_5() {
            var fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 5));
            assertEquals(esperado(5, 15), fs.score(), 0.01);
        }

        @Test @DisplayName("CogC = 10 → score 0.33")
        void cogc_10() {
            var fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 10));
            assertEquals(esperado(10, 15), fs.score(), 0.01);
        }

        @Test @DisplayName("CogC = 15 → score 0.00")
        void cogc_15() {
            var fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 15));
            assertEquals(0.0, fs.score());
        }

        @Test @DisplayName("CogC = 20 → score 0.00 (max trava em 0)")
        void cogc_20() {
            var fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 20));
            assertEquals(0.0, fs.score());
        }

        @Test @DisplayName("null → score 1.0 (sem métodos não penaliza)")
        void cogc_null() {
            assertEquals(1.0, scorer.scoreCognitive(null).score());
        }
    }

    // =========================================================================
    // Profundidade de Aninhamento  (limiar = 5)
    // =========================================================================
    @Nested
    @DisplayName("NestingDepth — score = max(0, 1 - depth / 5)")
    class NestingDepthTests {

        @Test @DisplayName("depth = 0 → score 1.00")
        void depth_0() {
            assertEquals(1.0, scorer.scoreNesting(result(NestingDepthFeature.NAME, 0)).score());
        }

        @Test @DisplayName("depth = 1 → score 0.80")
        void depth_1() {
            assertEquals(esperado(1, 5), scorer.scoreNesting(result(NestingDepthFeature.NAME, 1)).score(), 0.001);
        }

        @Test @DisplayName("depth = 3 → score 0.40")
        void depth_3() {
            assertEquals(esperado(3, 5), scorer.scoreNesting(result(NestingDepthFeature.NAME, 3)).score(), 0.001);
        }

        @Test @DisplayName("depth = 5 → score 0.00")
        void depth_5() {
            assertEquals(0.0, scorer.scoreNesting(result(NestingDepthFeature.NAME, 5)).score());
        }

        @Test @DisplayName("depth = 8 → score 0.00 (max trava em 0)")
        void depth_8() {
            assertEquals(0.0, scorer.scoreNesting(result(NestingDepthFeature.NAME, 8)).score());
        }
    }

    // =========================================================================
    // Comprimento de Identificadores  (zona ideal [8, 15])
    // =========================================================================
    @Nested
    @DisplayName("IdentifierLength — zona ideal [8, 15], zeros em 4 e 25")
    class IdentifierLengthTests {

        @Test @DisplayName("mean = 10 → score 1.0 (dentro do ideal)")
        void id_ideal() {
            assertEquals(1.0, scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 10)).score());
        }

        @Test @DisplayName("mean = 8  → score 1.0 (limite inferior do ideal)")
        void id_lowerBound() {
            assertEquals(1.0, scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 8)).score());
        }

        @Test @DisplayName("mean = 15 → score 1.0 (limite superior do ideal)")
        void id_upperBound() {
            assertEquals(1.0, scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 15)).score());
        }

        @Test @DisplayName("mean = 4  → score 0.0 (no limite inferior externo)")
        void id_zeroBelow() {
            assertEquals(0.0, scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 4)).score());
        }

        @Test @DisplayName("mean = 1  → score 0.0 (muito curto)")
        void id_tooShort() {
            assertEquals(0.0, scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 1)).score());
        }

        @Test @DisplayName("mean = 25 → score 0.0 (no limite superior externo)")
        void id_zeroAbove() {
            assertEquals(0.0, scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 25)).score());
        }

        @Test @DisplayName("mean = 6  → score entre 0 e 1 (rampa de subida)")
        void id_rampUp() {
            double s = scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 6)).score();
            assertTrue(s > 0.0 && s < 1.0, "score esperado entre 0 e 1, mas foi: " + s);
        }

        @Test @DisplayName("mean = 20 → score entre 0 e 1 (rampa de descida)")
        void id_rampDown() {
            double s = scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 20)).score();
            assertTrue(s > 0.0 && s < 1.0, "score esperado entre 0 e 1, mas foi: " + s);
        }
    }

    // =========================================================================
    // Número de Parâmetros  (limiar = 5)
    // =========================================================================
    @Nested
    @DisplayName("ParameterCount — score = max(0, 1 - params / 5)")
    class ParameterCountTests {

        @Test @DisplayName("params = 0 → score 1.00")
        void params_0() {
            assertEquals(1.0, scorer.scoreParameter(result(ParameterCountFeature.NAME, 0)).score());
        }

        @Test @DisplayName("params = 1 → score 0.80")
        void params_1() {
            assertEquals(esperado(1, 5), scorer.scoreParameter(result(ParameterCountFeature.NAME, 1)).score(), 0.001);
        }

        @Test @DisplayName("params = 3 → score 0.40")
        void params_3() {
            assertEquals(esperado(3, 5), scorer.scoreParameter(result(ParameterCountFeature.NAME, 3)).score(), 0.001);
        }

        @Test @DisplayName("params = 5 → score 0.00")
        void params_5() {
            assertEquals(0.0, scorer.scoreParameter(result(ParameterCountFeature.NAME, 5)).score());
        }

        @Test @DisplayName("params = 7 → score 0.00 (max trava em 0)")
        void params_7() {
            assertEquals(0.0, scorer.scoreParameter(result(ParameterCountFeature.NAME, 7)).score());
        }
    }

    // =========================================================================
    // Comprimento de Linha  (limiar = 100)
    // =========================================================================
    @Nested
    @DisplayName("LineLength — score = max(0, 1 - avgLen / 100)")
    class LineLengthTests {

        @Test @DisplayName("mean = 0   → score 1.00")
        void line_0() {
            assertEquals(1.0, scorer.scoreLine(result(LineLengthFeature.NAME, 0)).score());
        }

        @Test @DisplayName("mean = 50  → score 0.50")
        void line_50() {
            assertEquals(esperado(50, 100), scorer.scoreLine(result(LineLengthFeature.NAME, 50)).score(), 0.001);
        }

        @Test @DisplayName("mean = 80  → score 0.20")
        void line_80() {
            assertEquals(esperado(80, 100), scorer.scoreLine(result(LineLengthFeature.NAME, 80)).score(), 0.001);
        }

        @Test @DisplayName("mean = 100 → score 0.00")
        void line_100() {
            assertEquals(0.0, scorer.scoreLine(result(LineLengthFeature.NAME, 100)).score());
        }

        @Test @DisplayName("mean = 150 → score 0.00 (max trava em 0)")
        void line_150() {
            assertEquals(0.0, scorer.scoreLine(result(LineLengthFeature.NAME, 150)).score());
        }
    }

    // =========================================================================
    // Integração — nota final
    // =========================================================================
    @Nested
    @DisplayName("Integração — nota final ponderada")
    class IntegrationTests {

        @Test @DisplayName("todos os scores = 1.0 → nota final = 10.0")
        void perfectCode() {
            var results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 0),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        0),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    10),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      0),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,           0)
            );
            var sr = scorer.score(results);
            assertEquals(10.0, sr.finalScore());
            assertEquals("EXCELENTE", sr.classification());
        }

        @Test @DisplayName("todos os scores = 0.0 → nota final = 0.0")
        void terribleCode() {
            var results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 30),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        10),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    1),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      10),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,          200)
            );
            var sr = scorer.score(results);
            assertEquals(0.0, sr.finalScore());
            assertEquals("NECESSITA REFATORAÇÃO", sr.classification());
        }

        @Test @DisplayName("nota final sempre está em [0, 10]")
        void alwaysInRange() {
            var results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 999),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        999),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    999),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      999),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,          999)
            );
            var sr = scorer.score(results);
            assertTrue(sr.finalScore() >= 0.0 && sr.finalScore() <= 10.0);
        }

        @Test @DisplayName("ScoreResult tem exatamente 5 FeatureScores")
        void fiveDetails() {
            var results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 5),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        2),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    12),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      2),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,          70)
            );
            assertEquals(5, scorer.score(results).details().size());
        }

        @Test @DisplayName("nota calculada manualmente bate com a fórmula")
        void manualVerification() {
            // CogC=0 → 1.0, Nesting=0 → 1.0, Id=10 → 1.0, Params=0 → 1.0, Line=50 → 0.5
            // nota = (0.30*1 + 0.25*1 + 0.20*1 + 0.15*1 + 0.10*0.5) * 10
            //      = (0.30 + 0.25 + 0.20 + 0.15 + 0.05) * 10 = 0.95 * 10 = 9.5
            var results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 0),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        0),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    10),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      0),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,          50)
            );
            assertEquals(9.5, scorer.score(results).finalScore(), 0.01);
        }
    }

    // =========================================================================
    // Helpers internos
    // =========================================================================
    @Nested
    @DisplayName("Helpers internos")
    class HelperTests {

        @Test @DisplayName("decrescente(0, 15) = 1.0")
        void decrescenteZero() {
            assertEquals(1.0, Scorer.decrescente(0, 15));
        }

        @Test @DisplayName("decrescente(15, 15) = 0.0")
        void decrescenteNoLimiar() {
            assertEquals(0.0, Scorer.decrescente(15, 15));
        }

        @Test @DisplayName("decrescente(20, 15) = 0.0 (não fica negativo)")
        void decrescenteAcimaLimiar() {
            assertEquals(0.0, Scorer.decrescente(20, 15));
        }

        @Test @DisplayName("scoreZonaIdeal dentro do ideal → 1.0")
        void zonaIdealDentro() {
            assertEquals(1.0, Scorer.scoreZonaIdeal(10, 4, 8, 15, 25));
        }

        @Test @DisplayName("scoreZonaIdeal abaixo do zero → 0.0")
        void zonaIdealAbaixo() {
            assertEquals(0.0, Scorer.scoreZonaIdeal(2, 4, 8, 15, 25));
        }

        @Test @DisplayName("scoreZonaIdeal acima do zero → 0.0")
        void zonaIdealAcima() {
            assertEquals(0.0, Scorer.scoreZonaIdeal(30, 4, 8, 15, 25));
        }

        @Test @DisplayName("clamp abaixo do min → retorna min")
        void clampAbaixo() {
            assertEquals(0.0, Scorer.clamp(-1, 0, 10));
        }

        @Test @DisplayName("clamp acima do max → retorna max")
        void clampAcima() {
            assertEquals(10.0, Scorer.clamp(15, 0, 10));
        }
    }
}