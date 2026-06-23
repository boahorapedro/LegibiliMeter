package br.ufpe.cin.metric.reporter;

import br.ufpe.cin.metric.extractor.features.CognitiveComplexityFeature;
import br.ufpe.cin.metric.extractor.features.IdentifierLengthFeature;
import br.ufpe.cin.metric.extractor.features.LineLengthFeature;
import br.ufpe.cin.metric.extractor.features.NestingDepthFeature;
import br.ufpe.cin.metric.extractor.features.ParameterCountFeature;
import br.ufpe.cin.metric.scorer.FeatureScore;
import br.ufpe.cin.metric.scorer.ScoreResult;
import br.ufpe.cin.metric.scorer.Scorer;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Apresenta a nota final e o detalhamento por feature em três formatos:
 * texto para o terminal, JSON e CSV. Não tem dependências externas — todos os
 * formatos são montados manualmente.
 *
 * Os pesos exibidos são derivados das constantes do {@link Scorer} (fonte única
 * de verdade), para não divergirem após uma recalibração.
 */
public class Reporter {

    /** Rótulo legível de cada feature, na ordem de exibição. */
    private static final Map<String, String> LABELS = new LinkedHashMap<>();
    static {
        LABELS.put(CognitiveComplexityFeature.NAME, "Complexidade Cognitiva");
        LABELS.put(NestingDepthFeature.NAME,        "Profundidade de Aninhamento");
        LABELS.put(IdentifierLengthFeature.NAME,    "Comprimento de Identificadores");
        LABELS.put(ParameterCountFeature.NAME,      "Número de Parâmetros");
        LABELS.put(LineLengthFeature.NAME,          "Comprimento de Linha");
    }

    private static String label(String feature) {
        return LABELS.getOrDefault(feature, feature);
    }

    /** Peso (%) de cada feature, lido diretamente das constantes do {@link Scorer}. */
    private static int weight(String feature) {
        double w = switch (feature) {
            case CognitiveComplexityFeature.NAME -> Scorer.WEIGHT_COGNITIVE;
            case NestingDepthFeature.NAME        -> Scorer.WEIGHT_NESTING;
            case IdentifierLengthFeature.NAME    -> Scorer.WEIGHT_IDENTIFIER;
            case ParameterCountFeature.NAME      -> Scorer.WEIGHT_PARAMETER;
            case LineLengthFeature.NAME          -> Scorer.WEIGHT_LINE;
            default -> 0.0;
        };
        return (int) Math.round(w * 100);
    }

    // -------------------------------------------------------------------------
    // Terminal
    // -------------------------------------------------------------------------

    public String toTerminal(String fileName, ScoreResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append('\n');
        sb.append("  LegibiliMeter — ").append(fileName).append('\n');
        sb.append("=".repeat(60)).append('\n');
        sb.append(String.format(Locale.US, "  Nota final : %.2f / 10   [%s]%n",
                result.finalScore(), result.classification()));
        sb.append("-".repeat(60)).append('\n');
        sb.append(String.format("  %-32s %8s %8s %6s%n", "Feature", "Bruto", "Score", "Peso"));
        sb.append("-".repeat(60)).append('\n');
        for (FeatureScore fs : result.details()) {
            sb.append(String.format(Locale.US, "  %-32s %8.2f %8.2f %5d%%%n",
                    label(fs.feature()), fs.rawValue(), fs.score(), weight(fs.feature())));
        }
        sb.append("=".repeat(60)).append('\n');
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // JSON
    // -------------------------------------------------------------------------

    public String toJson(String fileName, ScoreResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"file\": \"").append(escapeJson(fileName)).append("\",\n");
        sb.append(String.format(Locale.US, "  \"finalScore\": %.2f,%n", result.finalScore()));
        sb.append("  \"classification\": \"").append(escapeJson(result.classification())).append("\",\n");
        sb.append("  \"features\": [\n");
        for (int i = 0; i < result.details().size(); i++) {
            FeatureScore fs = result.details().get(i);
            sb.append("    {\n");
            sb.append("      \"feature\": \"").append(escapeJson(fs.feature())).append("\",\n");
            sb.append("      \"label\": \"").append(escapeJson(label(fs.feature()))).append("\",\n");
            sb.append(String.format(Locale.US, "      \"rawValue\": %.2f,%n", fs.rawValue()));
            sb.append(String.format(Locale.US, "      \"score\": %.2f,%n", fs.score()));
            sb.append(String.format(Locale.US, "      \"weightPercent\": %d%n", weight(fs.feature())));
            sb.append("    }").append(i < result.details().size() - 1 ? "," : "").append('\n');
        }
        sb.append("  ]\n");
        sb.append("}\n");
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // CSV
    // -------------------------------------------------------------------------

    public String toCsv(String fileName, ScoreResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append("file,finalScore,classification,feature,label,rawValue,score,weightPercent\n");
        for (FeatureScore fs : result.details()) {
            sb.append(csv(fileName)).append(',')
                    .append(String.format(Locale.US, "%.2f", result.finalScore())).append(',')
                    .append(csv(result.classification())).append(',')
                    .append(csv(fs.feature())).append(',')
                    .append(csv(label(fs.feature()))).append(',')
                    .append(String.format(Locale.US, "%.2f", fs.rawValue())).append(',')
                    .append(String.format(Locale.US, "%.2f", fs.score())).append(',')
                    .append(weight(fs.feature()))
                    .append('\n');
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Helpers de escape
    // -------------------------------------------------------------------------

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static String csv(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
