package br.ufpe.cin.metric.scorer;

import br.ufpe.cin.metric.extractor.features.CognitiveComplexityFeature;
import br.ufpe.cin.metric.extractor.features.IdentifierLengthFeature;
import br.ufpe.cin.metric.extractor.features.LineLengthFeature;
import br.ufpe.cin.metric.extractor.features.NestingDepthFeature;
import br.ufpe.cin.metric.extractor.features.ParameterCountFeature;
import br.ufpe.cin.metric.model.FeatureResult;

import java.util.List;
import java.util.Map;

/**
 * Scorer: normaliza os valores brutos das 5 features em scores [0–1]
 * e calcula a nota final [0–10] pela soma ponderada.
 *
 * Fórmula de normalização (padrão para features "quanto menor, melhor"):
 *   score = max(0, 1 - valorBruto / limiar)
 *
 * Para IdentifierLength (tem zona ideal, não só "menor é melhor"),
 * usa penalização simétrica em torno do intervalo ideal.
 *
 * Pesos (após calibração Nível A — ver eval/DECISOES-CALIBRACAO.md):
 *   Complexidade Cognitiva   → 30%
 *   Profundidade Aninhamento → 20%   (era 25%)
 *   Compr. Identificadores   → 20%
 *   Número de Parâmetros     → 15%
 *   Comprimento de Linha     → 15%   (era 10%)
 *
 * Limiares:
 *   CognitiveComplexity : limiar = 15
 *   NestingDepth        : limiar = 6    (era 5)
 *   IdentifierLength    : ideal  = [8, 15]
 *   ParameterCount      : limiar = 5
 *   LineLength          : limiar = 100
 */
public class Scorer {

    // -------------------------------------------------------------------------
    // Pesos (somam 1.0)
    // Calibração Nível A: aninhamento penalizava forte demais frente ao
    // julgamento humano (snippet S05); parte do peso migrou para comprimento
    // de linha, melhor proxy para expressões densas/crípticas.
    // -------------------------------------------------------------------------
    public static final double WEIGHT_COGNITIVE  = 0.30;
    public static final double WEIGHT_NESTING    = 0.20;
    public static final double WEIGHT_IDENTIFIER = 0.20;
    public static final double WEIGHT_PARAMETER  = 0.15;
    public static final double WEIGHT_LINE       = 0.15;

    // -------------------------------------------------------------------------
    // Limiares (a partir desse valor o score chega a zero)
    // Calibração Nível A: limiar de aninhamento 5 → 6 para suavizar a punição
    // de código aninhado porém legível.
    // -------------------------------------------------------------------------
    static final double LIMIAR_COGNITIVE  = 15.0;
    static final double LIMIAR_NESTING    = 6.0;
    static final double LIMIAR_PARAMETER  = 5.0;
    static final double LIMIAR_LINE       = 100.0;

    // Comprimento de identificadores: zona ideal e limites externos
    static final double IDENTIFIER_IDEAL_MIN = 8.0;
    static final double IDENTIFIER_IDEAL_MAX = 15.0;
    static final double IDENTIFIER_MIN_ZERO  = 4.0;  // abaixo disso → score 0
    static final double IDENTIFIER_MAX_ZERO  = 25.0; // acima disso  → score 0

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    public ScoreResult score(Map<String, FeatureResult> results) {

        FeatureScore cogScore   = scoreCognitive(results.get(CognitiveComplexityFeature.NAME));
        FeatureScore nestScore  = scoreNesting(results.get(NestingDepthFeature.NAME));
        FeatureScore idScore    = scoreIdentifier(results.get(IdentifierLengthFeature.NAME));
        FeatureScore paramScore = scoreParameter(results.get(ParameterCountFeature.NAME));
        FeatureScore lineScore  = scoreLine(results.get(LineLengthFeature.NAME));

        double finalScore =
                (WEIGHT_COGNITIVE  * cogScore.score()   +
                        WEIGHT_NESTING    * nestScore.score()  +
                        WEIGHT_IDENTIFIER * idScore.score()    +
                        WEIGHT_PARAMETER  * paramScore.score() +
                        WEIGHT_LINE       * lineScore.score()) * 10.0;

        return new ScoreResult(
                round2(clamp(finalScore, 0.0, 10.0)),
                List.of(cogScore, nestScore, idScore, paramScore, lineScore)
        );
    }

    // -------------------------------------------------------------------------
    // Normalização por feature
    // -------------------------------------------------------------------------

    /**
     * score = max(0, 1 - CogC / 15)
     * Usa o max entre os métodos do arquivo (pior caso).
     */
    FeatureScore scoreCognitive(FeatureResult r) {
        if (r == null) return new FeatureScore(CognitiveComplexityFeature.NAME, 0, 1.0);
        double raw   = r.max();
        double score = decrescente(raw, LIMIAR_COGNITIVE);
        return new FeatureScore(CognitiveComplexityFeature.NAME, raw, round2(score));
    }

    /**
     * score = max(0, 1 - depth / 5)
     * Usa o max entre os métodos do arquivo (pior caso).
     */
    FeatureScore scoreNesting(FeatureResult r) {
        if (r == null) return new FeatureScore(NestingDepthFeature.NAME, 0, 1.0);
        double raw   = r.max();
        double score = decrescente(raw, LIMIAR_NESTING);
        return new FeatureScore(NestingDepthFeature.NAME, raw, round2(score));
    }

    /**
     * Comprimento de identificadores tem zona ideal [8, 15].
     * Dentro da zona → score 1.0.
     * Fora da zona   → cai linearmente até 0 nos limites externos.
     * Usa a média de todo o arquivo.
     */
    FeatureScore scoreIdentifier(FeatureResult r) {
        if (r == null) return new FeatureScore(IdentifierLengthFeature.NAME, 0, 0.0);
        double raw   = r.mean();
        double score = scoreZonaIdeal(raw,
                IDENTIFIER_MIN_ZERO, IDENTIFIER_IDEAL_MIN,
                IDENTIFIER_IDEAL_MAX, IDENTIFIER_MAX_ZERO);
        return new FeatureScore(IdentifierLengthFeature.NAME, raw, round2(score));
    }

    /**
     * Calcula o score baseado no número de parâmetros usando uma lógica em degrau:
     * ≤ 3 parâmetros → 1.0 (Ideal)
     * 4 a 5 parâmetros → 0.5 (Aceitável)
     * > 5 parâmetros → 0.0 (Problema de Design)
     * Uses o max entre os métodos do arquivo (pior caso).
     */
    FeatureScore scoreParameter(FeatureResult r) {
        if (r == null) return new FeatureScore(ParameterCountFeature.NAME, 0, 1.0);
        double raw = r.max();
        double score;

        if (raw <= LIMIAR_PARAMETER - 2) score = 1.0;
        else if (raw <= LIMIAR_PARAMETER) score = 0.5; // Ou outro valor de penalidade moderada
        else score = 0.0;

        return new FeatureScore(ParameterCountFeature.NAME, raw, round2(score));
    }

    /**
     * score = max(0, 1 - avgLineLength / 100)
     * Usa a média de todo o arquivo.
     */
    FeatureScore scoreLine(FeatureResult r) {
        if (r == null) return new FeatureScore(LineLengthFeature.NAME, 0, 1.0);
        double raw   = r.mean();
        double score = decrescente(raw, LIMIAR_LINE);
        return new FeatureScore(LineLengthFeature.NAME, raw, round2(score));
    }

    // -------------------------------------------------------------------------
    // Helpers de normalização
    // -------------------------------------------------------------------------

    /**
     * Fórmula padrão do grupo para features "quanto menor, melhor":
     *   score = max(0,  1 - valor / limiar)
     *
     * Exemplos com limiar=15:
     *   valor=0  → 1.00
     *   valor=5  → 0.67
     *   valor=15 → 0.00
     *   valor=20 → 0.00  (max trava em 0)
     */
    static double decrescente(double valor, double limiar) {
        return Math.max(0.0, 1.0 - valor / limiar);
    }

    /**
     * Score para features com zona ideal [idealMin, idealMax].
     *
     * Dentro da zona ideal                     → 1.0
     * Entre zeroAbaixo e idealMin (subindo)    → sobe de 0 até 1
     * Entre idealMax e zeroAcima  (descendo)   → desce de 1 até 0
     * Fora dos limites externos                → 0.0
     *
     * @param valor       valor bruto da feature
     * @param zeroAbaixo  valor abaixo do qual o score é 0
     * @param idealMin    início da zona ideal
     * @param idealMax    fim da zona ideal
     * @param zeroAcima   valor acima do qual o score é 0
     */
    static double scoreZonaIdeal(double valor,
                                 double zeroAbaixo, double idealMin,
                                 double idealMax,   double zeroAcima) {
        if (valor <= zeroAbaixo || valor >= zeroAcima) return 0.0;
        if (valor >= idealMin   && valor <= idealMax)  return 1.0;

        if (valor < idealMin) {
            // rampa de subida: zeroAbaixo → idealMin
            return (valor - zeroAbaixo) / (idealMin - zeroAbaixo);
        } else {
            // rampa de descida: idealMax → zeroAcima
            return (zeroAcima - valor) / (zeroAcima - idealMax);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers gerais
    // -------------------------------------------------------------------------

    static double clamp(double valor, double min, double max) {
        return Math.max(min, Math.min(max, valor));
    }

    static double round2(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}