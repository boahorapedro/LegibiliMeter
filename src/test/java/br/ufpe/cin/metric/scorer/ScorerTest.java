package br.ufpe.cin.metric.scorer;

import br.ufpe.cin.metric.extractor.features.*;
import br.ufpe.cin.metric.model.FeatureResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ScorerTest {

    private Scorer scorer;

    @BeforeEach
    void setUp() {
        scorer = new Scorer();
    }

    // =========================================================================
    // Complexidade Cognitiva
    // =========================================================================
    @Nested
    @DisplayName("CognitiveComplexity — normalização")
    class CognitiveComplexityTests {

        @Test
        @DisplayName("CogC = 0 → score 10 (código trivial)")
        void zeroComplexity() {
            FeatureScore fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 0));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("CogC = 5 → score 10 (limite superior do excelente)")
        void atIdealLimit() {
            FeatureScore fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 5));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("CogC = 10 → score 8 (limite da faixa boa)")
        void atGoodLimit() {
            FeatureScore fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 10));
            assertEquals(8.0, fs.score());
        }

        @Test
        @DisplayName("CogC = 15 → score 5 (limite aceitável)")
        void atAcceptableLimit() {
            FeatureScore fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 15));
            assertEquals(5.0, fs.score());
        }

        @Test
        @DisplayName("CogC = 16 → score < 5 (acima do limite crítico)")
        void aboveCritical() {
            FeatureScore fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 16));
            assertTrue(fs.score() < 5.0);
        }

        @Test
        @DisplayName("CogC = 30 → score 0 (muito acima do crítico)")
        void farAboveCritical() {
            FeatureScore fs = scorer.scoreCognitive(result(CognitiveComplexityFeature.NAME, 30));
            assertEquals(0.0, fs.score());
        }

        @Test
        @DisplayName("null → score 10 (arquivo sem métodos não penaliza)")
        void nullResult() {
            FeatureScore fs = scorer.scoreCognitive(null);
            assertEquals(10.0, fs.score());
        }
    }

    // =========================================================================
    // Profundidade de Aninhamento
    // =========================================================================
    @Nested
    @DisplayName("NestingDepth — normalização")
    class NestingDepthTests {

        @Test
        @DisplayName("depth = 1 → score 10 (excelente)")
        void shallowNesting() {
            FeatureScore fs = scorer.scoreNesting(result(NestingDepthFeature.NAME, 1));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("depth = 2 → score 10 (limite excelente)")
        void atExcellentLimit() {
            FeatureScore fs = scorer.scoreNesting(result(NestingDepthFeature.NAME, 2));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("depth = 3 → score 7 (aceitável, limite Linus Torvalds)")
        void atAcceptableLimit() {
            FeatureScore fs = scorer.scoreNesting(result(NestingDepthFeature.NAME, 3));
            assertEquals(7.0, fs.score());
        }

        @Test
        @DisplayName("depth = 4 → score 4 (problemático)")
        void atProblematicLimit() {
            FeatureScore fs = scorer.scoreNesting(result(NestingDepthFeature.NAME, 4));
            assertEquals(4.0, fs.score());
        }

        @Test
        @DisplayName("depth = 5 → score ≤ 2 (crítico)")
        void atCriticalLimit() {
            FeatureScore fs = scorer.scoreNesting(result(NestingDepthFeature.NAME, 5));
            assertTrue(fs.score() <= 2.0);
        }

        @Test
        @DisplayName("depth ≥ 6 → score 0")
        void beyondCritical() {
            FeatureScore fs = scorer.scoreNesting(result(NestingDepthFeature.NAME, 6));
            assertEquals(0.0, fs.score());
        }
    }

    // =========================================================================
    // Comprimento de Identificadores
    // =========================================================================
    @Nested
    @DisplayName("IdentifierLength — normalização")
    class IdentifierLengthTests {

        @Test
        @DisplayName("mean = 10 → score 10 (zona ideal 8–15)")
        void inIdealRange() {
            FeatureScore fs = scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 10));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("mean = 8 → score 10 (limite inferior do ideal)")
        void atLowerIdealBound() {
            FeatureScore fs = scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 8));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("mean = 15 → score 10 (limite superior do ideal)")
        void atUpperIdealBound() {
            FeatureScore fs = scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 15));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("mean = 3 → score 0 (muito curto → críptico)")
        void tooShort() {
            FeatureScore fs = scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 3));
            assertEquals(0.0, fs.score());
        }

        @Test
        @DisplayName("mean = 25 → score 0 (muito longo → prejudica escaneabilidade)")
        void tooLong() {
            FeatureScore fs = scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 25));
            assertEquals(0.0, fs.score());
        }

        @Test
        @DisplayName("mean = 7 → score entre 0 e 10 (zona de penalização leve)")
        void slightlyShort() {
            FeatureScore fs = scorer.scoreIdentifier(result(IdentifierLengthFeature.NAME, 7));
            assertTrue(fs.score() > 0.0 && fs.score() < 10.0);
        }
    }

    // =========================================================================
    // Número de Parâmetros
    // =========================================================================
    @Nested
    @DisplayName("ParameterCount — normalização")
    class ParameterCountTests {

        @Test
        @DisplayName("max = 0 → score 10 (niládico — ideal máximo)")
        void noParams() {
            FeatureScore fs = scorer.scoreParameter(result(ParameterCountFeature.NAME, 0));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("max = 3 → score 10 (triádico — último ideal)")
        void threeParams() {
            FeatureScore fs = scorer.scoreParameter(result(ParameterCountFeature.NAME, 3));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("max = 4 → score < 10 (começa a penalizar)")
        void fourParams() {
            FeatureScore fs = scorer.scoreParameter(result(ParameterCountFeature.NAME, 4));
            assertTrue(fs.score() < 10.0 && fs.score() > 0.0);
        }

        @Test
        @DisplayName("max = 5 → score 2 (aceitável com ressalvas)")
        void fiveParams() {
            FeatureScore fs = scorer.scoreParameter(result(ParameterCountFeature.NAME, 5));
            assertEquals(2.0, fs.score());
        }

        @Test
        @DisplayName("max > 5 → score próximo de 0 (problema de design)")
        void sixParams() {
            FeatureScore fs = scorer.scoreParameter(result(ParameterCountFeature.NAME, 6));
            assertTrue(fs.score() < 2.0);
        }

        @Test
        @DisplayName("max = 10 → score 0 (poliádico extremo)")
        void tenParams() {
            FeatureScore fs = scorer.scoreParameter(result(ParameterCountFeature.NAME, 10));
            assertEquals(0.0, fs.score());
        }
    }

    // =========================================================================
    // Comprimento de Linha
    // =========================================================================
    @Nested
    @DisplayName("LineLength — normalização")
    class LineLengthTests {

        @Test
        @DisplayName("mean = 50 → score 10 (bem abaixo do limite)")
        void shortLines() {
            FeatureScore fs = scorer.scoreLine(result(LineLengthFeature.NAME, 50));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("mean = 80 → score 10 (exatamente no limite excelente)")
        void atExcellentLimit() {
            FeatureScore fs = scorer.scoreLine(result(LineLengthFeature.NAME, 80));
            assertEquals(10.0, fs.score());
        }

        @Test
        @DisplayName("mean = 100 → score 5 (limite aceitável)")
        void atAcceptableLimit() {
            FeatureScore fs = scorer.scoreLine(result(LineLengthFeature.NAME, 100));
            assertEquals(5.0, fs.score());
        }

        @Test
        @DisplayName("mean = 90 → score entre 5 e 10 (zona aceitável)")
        void inAcceptableRange() {
            FeatureScore fs = scorer.scoreLine(result(LineLengthFeature.NAME, 90));
            assertTrue(fs.score() > 5.0 && fs.score() < 10.0);
        }

        @Test
        @DisplayName("mean = 150 → score 0 (penalização máxima)")
        void veryLongLines() {
            FeatureScore fs = scorer.scoreLine(result(LineLengthFeature.NAME, 150));
            assertEquals(0.0, fs.score());
        }
    }

    // =========================================================================
    // Integração — nota final ponderada
    // =========================================================================
    @Nested
    @DisplayName("Integração — nota final")
    class IntegrationTests {

        @Test
        @DisplayName("código ideal em todas as features → nota 10")
        void perfectCode() {
            Map<String, FeatureResult> results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 2),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        1),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    10),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      1),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,          60)
            );
            ScoreResult sr = scorer.score(results);
            assertEquals(10.0, sr.finalScore());
            assertEquals("EXCELENTE", sr.classification());
        }

        @Test
        @DisplayName("código péssimo em todas as features → nota próxima de 0")
        void terribleCode() {
            Map<String, FeatureResult> results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 30),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        8),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    2),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      10),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,          200)
            );
            ScoreResult sr = scorer.score(results);
            assertEquals(0.0, sr.finalScore());
            assertEquals("NECESSITA REFATORAÇÃO", sr.classification());
        }

        @Test
        @DisplayName("nota final sempre está no intervalo [0, 10]")
        void scoreAlwaysInRange() {
            Map<String, FeatureResult> results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 100),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        100),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    100),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      100),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,          1000)
            );
            ScoreResult sr = scorer.score(results);
            assertTrue(sr.finalScore() >= 0.0 && sr.finalScore() <= 10.0);
        }

        @Test
        @DisplayName("ScoreResult contém exatamente 5 FeatureScores")
        void detailsHaveFiveEntries() {
            Map<String, FeatureResult> results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 5),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        2),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    12),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      2),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,          75)
            );
            ScoreResult sr = scorer.score(results);
            assertEquals(5, sr.details().size());
        }

        @Test
        @DisplayName("Classificação ACEITÁVEL para nota entre 4 e 6")
        void classificationAcceptable() {
            // CogC moderado + nesting ok + resto ideal → nota na faixa aceitável
            Map<String, FeatureResult> results = Map.of(
                    CognitiveComplexityFeature.NAME, result(CognitiveComplexityFeature.NAME, 14),
                    NestingDepthFeature.NAME,        result(NestingDepthFeature.NAME,        4),
                    IdentifierLengthFeature.NAME,    result(IdentifierLengthFeature.NAME,    10),
                    ParameterCountFeature.NAME,      result(ParameterCountFeature.NAME,      3),
                    LineLengthFeature.NAME,          result(LineLengthFeature.NAME,          80)
            );
            ScoreResult sr = scorer.score(results);
            assertTrue(sr.finalScore() >= 4.0 && sr.finalScore() < 8.0,
                    "Esperava nota entre 4 e 8, mas foi: " + sr.finalScore());
        }
    }

    // =========================================================================
    // Helpers matemáticos internos
    // =========================================================================
    @Nested
    @DisplayName("Helpers matemáticos")
    class MathHelperTests {

        @Test
        @DisplayName("clamp abaixo do min → retorna min")
        void clampBelowMin() {
            assertEquals(0.0, Scorer.clamp(-5, 0, 10));
        }

        @Test
        @DisplayName("clamp acima do max → retorna max")
        void clampAboveMax() {
            assertEquals(10.0, Scorer.clamp(15, 0, 10));
        }

        @Test
        @DisplayName("linearDescending no ponto inicial → retorna scoreHi")
        void linearDescStart() {
            assertEquals(10.0, Scorer.linearDescending(5, 5, 10, 10.0, 0.0));
        }

        @Test
        @DisplayName("linearDescending no ponto final → retorna scoreLo")
        void linearDescEnd() {
            assertEquals(0.0, Scorer.linearDescending(10, 5, 10, 10.0, 0.0));
        }

        @Test
        @DisplayName("linearAscending no ponto médio → retorna valor médio")
        void linearAscMid() {
            double result = Scorer.linearAscending(5, 0, 10, 0.0, 10.0);
            assertEquals(5.0, result, 0.001);
        }
    }

    // =========================================================================
    // Utilitário
    // =========================================================================

    /** Cria um FeatureResult simples (max == mean == value). */
    private static FeatureResult result(String name, double value) {
        return FeatureResult.single(name, value);
    }
}