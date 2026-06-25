package br.ufpe.cin.metric.eval;

import br.ufpe.cin.metric.extractor.FeatureExtractor;
import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;
import br.ufpe.cin.metric.parser.JavaFileLoader;
import br.ufpe.cin.metric.scorer.ScoreResult;
import br.ufpe.cin.metric.scorer.Scorer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Harness de avaliação (Semana 5): compara a nota do LegibiliMeter com a nota
 * média atribuída pela turma a cada snippet na inspeção de código.
 *
 * Para cada arquivo {@code Sxx-*.java} no diretório de snippets:
 *   1. roda a pipeline (extração → score) e obtém a nota da ferramenta [0–10];
 *   2. lê a nota média humana [1–5] do CSV de notas da turma;
 *   3. ao final, reporta a correlação de Pearson e de Spearman entre as duas séries.
 *
 * Uso:
 *   java EvaluationRunner &lt;dirSnippets&gt; &lt;classScores.csv&gt; [saidaBase]
 *
 * Se {@code saidaBase} for informado, grava automaticamente dois relatórios:
 * {@code <base>.csv} e {@code <base>.json} (metadados + correlações + tabela
 * por snippet com resíduo). A correlação também é impressa no terminal.
 *
 * O critério da proposta (Seção 3.2) é satisfeito se Spearman ρ &gt; 0,6.
 */
public class EvaluationRunner {

    /** Uma linha de comparação: snippet, nota da ferramenta e nota da turma. */
    public record Row(String snippet, double toolScore, double classMean) {}

    /** Limiar de Spearman exigido pela Seção 3.2 da proposta. */
    static final double SPEARMAN_THRESHOLD = 0.6;

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Uso: java EvaluationRunner <dirSnippets> <classScores.csv> [saidaBase]");
            System.out.println("  saidaBase: caminho-base; gera <base>.csv e <base>.json");
            return;
        }
        Path snippetsDir = Path.of(args[0]);
        Path classScoresCsv = Path.of(args[1]);
        String outputBase = args.length >= 3 ? stripExtension(args[2]) : null;

        Map<String, Double> classMeans = readClassScores(classScoresCsv);
        List<Row> rows = scoreSnippets(snippetsDir, classMeans);

        printTable(rows);

        double[] tool = rows.stream().mapToDouble(Row::toolScore).toArray();
        double[] human = rows.stream().mapToDouble(Row::classMean).toArray();
        double pearson = pearson(tool, human);
        double spearman = spearman(tool, human);

        printCorrelations(rows.size(), pearson, spearman);

        if (outputBase != null) {
            Path csv = Path.of(outputBase + ".csv");
            Path json = Path.of(outputBase + ".json");
            writeCsv(csv, rows);
            writeJson(json, snippetsDir, rows, pearson, spearman);
            System.out.println("\nRelatórios gravados em:");
            System.out.println("  CSV : " + csv);
            System.out.println("  JSON: " + json);
        }
    }

    /** Remove a extensão (.csv/.json/…) de um caminho para usá-lo como base. */
    static String stripExtension(String path) {
        int dot = path.lastIndexOf('.');
        int sep = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return dot > sep ? path.substring(0, dot) : path;
    }

    // -------------------------------------------------------------------------
    // Coleta
    // -------------------------------------------------------------------------

    /** Lê o CSV {@code snippet,classMean,...} e devolve o mapa snippet → média da turma. */
    static Map<String, Double> readClassScores(Path csv) throws IOException {
        Map<String, Double> means = new HashMap<>();
        List<String> lines = Files.readAllLines(csv);
        for (int i = 1; i < lines.size(); i++) {            // pula o cabeçalho
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] cols = line.split(",");
            means.put(cols[0], Double.parseDouble(cols[1]));
        }
        return means;
    }

    /** Roda a pipeline em cada snippet e casa com a média da turma pelo prefixo {@code Sxx}. */
    static List<Row> scoreSnippets(Path snippetsDir, Map<String, Double> classMeans) throws IOException {
        FeatureExtractor extractor = new FeatureExtractor();
        Scorer scorer = new Scorer();

        List<Path> files;
        try (var stream = Files.list(snippetsDir)) {
            files = stream
                    .filter(p -> p.getFileName().toString().endsWith(".java"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
        }

        List<Row> rows = new ArrayList<>();
        for (Path file : files) {
            String id = snippetId(file.getFileName().toString());
            Double classMean = classMeans.get(id);
            if (classMean == null) {
                System.err.println("Aviso: sem nota da turma para " + id + " — ignorado.");
                continue;
            }
            try {
                SourceFile source = JavaFileLoader.loadSource(file.toString());
                Map<String, FeatureResult> raw = extractor.extractAll(source);
                ScoreResult result = scorer.score(raw);
                rows.add(new Row(id, result.finalScore(), classMean));
            } catch (RuntimeException e) {
                System.err.println("Aviso: " + id + " não é Java parseável — excluído da correlação.");
            }
        }
        return rows;
    }

    /** "S01-snippet-01-40.java" → "S01". */
    static String snippetId(String fileName) {
        int dash = fileName.indexOf('-');
        return dash > 0 ? fileName.substring(0, dash) : fileName.replace(".java", "");
    }

    // -------------------------------------------------------------------------
    // Estatística
    // -------------------------------------------------------------------------

    /** Correlação de Pearson entre dois vetores de mesmo tamanho. */
    static double pearson(double[] x, double[] y) {
        int n = x.length;
        double mx = mean(x), my = mean(y);
        double num = 0, dx = 0, dy = 0;
        for (int i = 0; i < n; i++) {
            double a = x[i] - mx, b = y[i] - my;
            num += a * b;
            dx += a * a;
            dy += b * b;
        }
        double den = Math.sqrt(dx * dy);
        return den == 0 ? 0 : num / den;
    }

    /** Correlação de Spearman: Pearson aplicado aos postos (com média de postos para empates). */
    static double spearman(double[] x, double[] y) {
        return pearson(ranks(x), ranks(y));
    }

    /** Postos [1..n] de um vetor, atribuindo a média dos postos a valores empatados. */
    static double[] ranks(double[] values) {
        int n = values.length;
        Integer[] idx = new Integer[n];
        for (int i = 0; i < n; i++) idx[i] = i;
        Arrays.sort(idx, Comparator.comparingDouble(i -> values[i]));

        double[] ranks = new double[n];
        int i = 0;
        while (i < n) {
            int j = i;
            while (j + 1 < n && values[idx[j + 1]] == values[idx[i]]) j++;
            double avgRank = (i + j) / 2.0 + 1;             // postos 1-based, média no empate
            for (int k = i; k <= j; k++) ranks[idx[k]] = avgRank;
            i = j + 1;
        }
        return ranks;
    }

    static double mean(double[] v) {
        double s = 0;
        for (double d : v) s += d;
        return s / v.length;
    }

    // -------------------------------------------------------------------------
    // Saída
    // -------------------------------------------------------------------------

    static void printTable(List<Row> rows) {
        System.out.println("=".repeat(64));
        System.out.println("  Avaliação LegibiliMeter × Nota da turma (inspeção de código)");
        System.out.println("=".repeat(64));
        System.out.printf("  %-8s %12s %12s %14s%n",
                "Snippet", "Ferramenta", "Turma(1-5)", "Turma(0-10)");
        System.out.println("-".repeat(64));
        for (Row r : rows) {
            System.out.printf(Locale.US, "  %-8s %12.2f %12.2f %14.2f%n",
                    r.snippet(), r.toolScore(), r.classMean(), r.classMean() * 2.0);
        }
        System.out.println("-".repeat(64));
    }

    static void printCorrelations(int n, double pearson, double spearman) {
        System.out.printf(Locale.US, "  N = %d snippets%n", n);
        System.out.printf(Locale.US, "  Pearson  r = %.3f%n", pearson);
        System.out.printf(Locale.US, "  Spearman ρ = %.3f   (critério da proposta: ρ > 0,6 → %s)%n",
                spearman, spearman > SPEARMAN_THRESHOLD ? "ATENDIDO" : "NÃO atendido");
        System.out.println("=".repeat(64));
    }

    static void writeCsv(Path out, List<Row> rows) throws IOException {
        StringBuilder sb = new StringBuilder("snippet,toolScore,classMean,classMeanScaled0_10,residual\n");
        for (Row r : rows) {
            double scaled = r.classMean() * 2.0;
            sb.append(String.format(Locale.US, "%s,%.2f,%.4f,%.2f,%.2f%n",
                    r.snippet(), r.toolScore(), r.classMean(), scaled, r.toolScore() - scaled));
        }
        Files.writeString(out, sb.toString());
    }

    /**
     * Relatório estruturado em JSON: metadados, correlações e tabela por snippet
     * (com resíduo). Montado manualmente, sem dependências externas.
     */
    static void writeJson(Path out, Path snippetsDir, List<Row> rows,
                          double pearson, double spearman) throws IOException {
        boolean met = spearman > SPEARMAN_THRESHOLD;
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"generatedAt\": \"").append(Instant.now()).append("\",\n");
        sb.append("  \"dataset\": {\n");
        sb.append("    \"snippetsDir\": \"").append(escapeJson(snippetsDir.toString())).append("\",\n");
        sb.append("    \"nEvaluated\": ").append(rows.size()).append("\n");
        sb.append("  },\n");
        sb.append("  \"correlations\": {\n");
        sb.append(String.format(Locale.US, "    \"pearson\": %.4f,%n", pearson));
        sb.append(String.format(Locale.US, "    \"spearman\": %.4f,%n", spearman));
        sb.append(String.format(Locale.US, "    \"spearmanThreshold\": %.2f,%n", SPEARMAN_THRESHOLD));
        sb.append("    \"spearmanCriterionMet\": ").append(met).append("\n");
        sb.append("  },\n");
        sb.append("  \"results\": [\n");
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            double scaled = r.classMean() * 2.0;
            sb.append("    {");
            sb.append("\"snippet\": \"").append(escapeJson(r.snippet())).append("\", ");
            sb.append(String.format(Locale.US, "\"toolScore\": %.2f, ", r.toolScore()));
            sb.append(String.format(Locale.US, "\"classMean\": %.4f, ", r.classMean()));
            sb.append(String.format(Locale.US, "\"classMeanScaled0_10\": %.2f, ", scaled));
            sb.append(String.format(Locale.US, "\"residual\": %.2f", r.toolScore() - scaled));
            sb.append("}").append(i < rows.size() - 1 ? "," : "").append('\n');
        }
        sb.append("  ]\n");
        sb.append("}\n");
        Files.writeString(out, sb.toString());
    }

    private static String escapeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
