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
 * Scorer: normaliza os valores brutos das 5 features em scores [0–10]
 * e calcula a nota final pela soma ponderada definida no projeto.
 *
 * Pesos:
 *   Complexidade Cognitiva  → 30%
 *   Profundidade Aninhamento→ 25%
 *   Compr. Identificadores  → 20%
 *   Número de Parâmetros    → 15%
 *   Comprimento de Linha    → 10%
 *
 * Limiares (baseados no PDF da proposta):
 *   CognitiveComplexity : ideal ≤ 5   | crítico > 15
 *   NestingDepth        : ideal ≤ 2   | crítico ≥ 5
 *   IdentifierLength    : ideal 8–15  | fora desse intervalo penaliza
 *   ParameterCount      : ideal ≤ 3   | crítico > 5
 *   LineLength          : ideal ≤ 80  | crítico > 100
 */
public class Scorer {

    // -------------------------------------------------------------------------
    // Pesos (somam 1.0)
    // -------------------------------------------------------------------------
    static final double WEIGHT_COGNITIVE   = 0.30;
    static final double WEIGHT_NESTING     = 0.25;
    static final double WEIGHT_IDENTIFIER  = 0.20;
    static final double WEIGHT_PARAMETER   = 0.15;
    static final double WEIGHT_LINE        = 0.10;

    // -------------------------------------------------------------------------
    // API pública
    // -------------------------------------------------------------------------

    /**
     * Calcula o {@link ScoreResult} a partir dos resultados brutos das features.
     *
     * @param results mapa de nome-da-feature → FeatureResult
     * @return ScoreResult com nota final e detalhamento
     */
    public ScoreResult score(Map<String, FeatureResult> results) {

        FeatureScore cogScore   = scoreCognitive(results.get(CognitiveComplexityFeature.NAME));
        FeatureScore nestScore  = scoreNesting(results.get(NestingDepthFeature.NAME));
        FeatureScore idScore    = scoreIdentifier(results.get(IdentifierLengthFeature.NAME));
        FeatureScore paramScore = scoreParameter(results.get(ParameterCountFeature.NAME));
        FeatureScore lineScore  = scoreLine(results.get(LineLengthFeature.NAME));

        double finalScore =
                WEIGHT_COGNITIVE  * cogScore.score()   +
                        WEIGHT_NESTING    * nestScore.score()  +
                        WEIGHT_IDENTIFIER * idScore.score()    +
                        WEIGHT_PARAMETER  * paramScore.score() +
                        WEIGHT_LINE       * lineScore.score();

        // garante que a nota fique exatamente em [0, 10]
        finalScore = clamp(finalScore, 0.0, 10.0);

        return new ScoreResult(
                round2(finalScore),
                List.of(cogScore, nestScore, idScore, paramScore, lineScore)
        );
    }

    // -------------------------------------------------------------------------
    // Normalização por feature
    // -------------------------------------------------------------------------

    /**
     * Complexidade Cognitiva (usa o max entre os métodos do arquivo).
     *   ≤ 5   → 10.0  (EXCELENTE)
     *   6–10  → 8.0   (BOA)
     *   11–15 → 5.0   (ACEITÁVEL)
     *   > 15  → 0.0   (CRÍTICO — refatoração obrigatória)
     */
    FeatureScore scoreCognitive(FeatureResult r) {
        if (r == null) return new FeatureScore(CognitiveComplexityFeature.NAME, 0, 10.0);
        double raw = r.max();
        double score;
        if (raw <= 5)       score = 10.0;
        else if (raw <= 10) score = linearDescending(raw, 5, 10, 10.0, 8.0);
        else if (raw <= 15) score = linearDescending(raw, 10, 15, 8.0, 5.0);
        else                score = linearDescending(raw, 15, 30, 5.0, 0.0);
        return new FeatureScore(CognitiveComplexityFeature.NAME, raw, round2(clamp(score, 0, 10)));
    }

    /**
     * Profundidade de Aninhamento (usa o max entre os métodos do arquivo).
     *   ≤ 2  → 10.0  (EXCELENTE)
     *   3    → 7.0   (ACEITÁVEL)
     *   4    → 4.0   (PROBLEMÁTICO)
     *   ≥ 5  → 0.0   (CRÍTICO)
     */
    FeatureScore scoreNesting(FeatureResult r) {
        if (r == null) return new FeatureScore(NestingDepthFeature.NAME, 0, 10.0);
        double raw = r.max();
        double score;
        if (raw <= 2)      score = 10.0;
        else if (raw <= 3) score = linearDescending(raw, 2, 3, 10.0, 7.0);
        else if (raw <= 4) score = linearDescending(raw, 3, 4, 7.0, 4.0);
        else               score = linearDescending(raw, 4, 6, 4.0, 0.0);
        return new FeatureScore(NestingDepthFeature.NAME, raw, round2(clamp(score, 0, 10)));
    }

    /**
     * Comprimento Médio de Identificadores (usa a média de todo o arquivo).
     * Zona ideal: 8–15 caracteres.
     *   8–15  → 10.0
     *   6–8   → interpolação crescente  (muito curto)
     *   15–20 → interpolação decrescente (muito longo)
     *   < 6   → 0.0
     *   > 20  → 0.0
     */
    FeatureScore scoreIdentifier(FeatureResult r) {
        if (r == null) return new FeatureScore(IdentifierLengthFeature.NAME, 0, 0.0);
        double raw = r.mean();
        double score;
        if (raw < 6)        score = 0.0;
        else if (raw < 8)   score = linearAscending(raw, 6, 8, 0.0, 10.0);
        else if (raw <= 15) score = 10.0;
        else if (raw <= 20) score = linearDescending(raw, 15, 20, 10.0, 0.0);
        else                score = 0.0;
        return new FeatureScore(IdentifierLengthFeature.NAME, raw, round2(clamp(score, 0, 10)));
    }

    /**
     * Número de Parâmetros (usa o max entre os métodos do arquivo).
     *   ≤ 3  → 10.0  (IDEAL)
     *   4–5  → interpolação (ACEITÁVEL)
     *   > 5  → 0.0   (PROBLEMA DE DESIGN)
     */
    FeatureScore scoreParameter(FeatureResult r) {
        if (r == null) return new FeatureScore(ParameterCountFeature.NAME, 0, 10.0);
        double raw = r.max();
        double score;
        if (raw <= 3)      score = 10.0;
        else if (raw <= 5) score = linearDescending(raw, 3, 5, 10.0, 2.0);
        else               score = linearDescending(raw, 5, 10, 2.0, 0.0);
        return new FeatureScore(ParameterCountFeature.NAME, raw, round2(clamp(score, 0, 10)));
    }

    /**
     * Comprimento Médio de Linha (usa a média de todo o arquivo).
     *   ≤ 80  → 10.0  (EXCELENTE)
     *   81–100→ interpolação (ACEITÁVEL)
     *   > 100 → penalização progressiva
     */
    FeatureScore scoreLine(FeatureResult r) {
        if (r == null) return new FeatureScore(LineLengthFeature.NAME, 0, 10.0);
        double raw = r.mean();
        double score;
        if (raw <= 80)       score = 10.0;
        else if (raw <= 100) score = linearDescending(raw, 80, 100, 10.0, 5.0);
        else                 score = linearDescending(raw, 100, 150, 5.0, 0.0);
        return new FeatureScore(LineLengthFeature.NAME, raw, round2(clamp(score, 0, 10)));
    }

    // -------------------------------------------------------------------------
    // Helpers matemáticos
    // -------------------------------------------------------------------------

    /** Interpolação linear decrescente: mapeia [lo, hi] → [scoreHi, scoreLo]. */
    static double linearDescending(double x, double lo, double hi,
                                   double scoreHi, double scoreLo) {
        if (x <= lo) return scoreHi;
        if (x >= hi) return scoreLo;
        double t = (x - lo) / (hi - lo);
        return scoreHi + t * (scoreLo - scoreHi);
    }

    /** Interpolação linear crescente: mapeia [lo, hi] → [scoreLo, scoreHi]. */
    static double linearAscending(double x, double lo, double hi,
                                  double scoreLo, double scoreHi) {
        if (x <= lo) return scoreLo;
        if (x >= hi) return scoreHi;
        double t = (x - lo) / (hi - lo);
        return scoreLo + t * (scoreHi - scoreLo);
    }

    static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}