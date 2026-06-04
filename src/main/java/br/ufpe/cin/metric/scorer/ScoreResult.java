package br.ufpe.cin.metric.scorer;

import java.util.List;

/**
 * Resultado completo da avaliação: nota final [0–10] e detalhamento por feature.
 */
public record ScoreResult(double finalScore, List<FeatureScore> details) {

    public String classification() {
        if (finalScore >= 8.0) return "EXCELENTE";
        if (finalScore >= 6.0) return "BOA LEGIBILIDADE";
        if (finalScore >= 4.0) return "ACEITÁVEL";
        return "NECESSITA REFATORAÇÃO";
    }
}