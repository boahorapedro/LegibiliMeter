package br.ufpe.cin.metric.eval;

import br.ufpe.cin.metric.extractor.FeatureExtractor;
import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;
import br.ufpe.cin.metric.parser.JavaFileLoader;
import br.ufpe.cin.metric.scorer.ScoreResult;
import br.ufpe.cin.metric.scorer.Scorer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Smoke test (Seção 3.2): roda o scorer sobre os exemplos sintéticos de controle
 * — código "ideal" e "problemático" escritos pelo grupo — e verifica se a nota
 * cai na faixa esperada:
 *
 *   código ideal        → nota &gt; 7,0
 *   código problemático → nota &lt; 4,0
 *
 * Estrutura esperada do diretório:
 *   &lt;controlDir&gt;/ideal/*.java
 *   &lt;controlDir&gt;/problematic/*.java
 *
 * Uso:
 *   java ControlRunner &lt;controlDir&gt; [saida.csv]
 *
 * A mesma lógica é reutilizada por {@code SmokeTest} para travar o critério
 * na suíte de testes.
 */
public class ControlRunner {

    public static final double IDEAL_MIN_SCORE       = 7.0;
    public static final double PROBLEMATIC_MAX_SCORE = 4.0;

    public static final String IDEAL       = "ideal";
    public static final String PROBLEMATIC = "problematic";

    /** Resultado do smoke test para um exemplo de controle. */
    public record ControlRow(String sample, String category, double toolScore) {
        public double threshold() {
            return category.equals(IDEAL) ? IDEAL_MIN_SCORE : PROBLEMATIC_MAX_SCORE;
        }
        public boolean pass() {
            return category.equals(IDEAL)
                    ? toolScore > IDEAL_MIN_SCORE
                    : toolScore < PROBLEMATIC_MAX_SCORE;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Uso: java ControlRunner <controlDir> [saida.csv]");
            return;
        }
        Path controlDir = Path.of(args[0]);
        Path outputCsv = args.length >= 2 ? Path.of(args[1]) : null;

        List<ControlRow> rows = scoreControl(controlDir);
        printTable(rows);

        if (outputCsv != null) {
            writeCsv(outputCsv, rows);
            System.out.println("\nResultado do smoke test gravado em: " + outputCsv);
        }
    }

    /** Pontua todos os exemplos de {@code controlDir/ideal} e {@code controlDir/problematic}. */
    public static List<ControlRow> scoreControl(Path controlDir) {
        List<ControlRow> rows = new ArrayList<>();
        rows.addAll(scoreCategory(controlDir.resolve(IDEAL), IDEAL));
        rows.addAll(scoreCategory(controlDir.resolve(PROBLEMATIC), PROBLEMATIC));
        return rows;
    }

    private static List<ControlRow> scoreCategory(Path dir, String category) {
        FeatureExtractor extractor = new FeatureExtractor();
        Scorer scorer = new Scorer();
        List<ControlRow> rows = new ArrayList<>();
        try (var stream = Files.list(dir)) {
            List<Path> files = stream
                    .filter(p -> p.getFileName().toString().endsWith(".java"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .toList();
            for (Path file : files) {
                SourceFile source = JavaFileLoader.loadSource(file.toString());
                Map<String, FeatureResult> raw = extractor.extractAll(source);
                ScoreResult result = scorer.score(raw);
                String name = file.getFileName().toString().replace(".java", "");
                rows.add(new ControlRow(name, category, result.finalScore()));
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler diretório de controle: " + dir, e);
        }
        return rows;
    }

    // -------------------------------------------------------------------------
    // Saída
    // -------------------------------------------------------------------------

    static void printTable(List<ControlRow> rows) {
        System.out.println("=".repeat(60));
        System.out.println("  Smoke test — exemplos sintéticos de controle");
        System.out.println("=".repeat(60));
        System.out.printf("  %-18s %-13s %8s %8s %6s%n",
                "Exemplo", "Categoria", "Nota", "Limiar", "OK?");
        System.out.println("-".repeat(60));
        int passed = 0;
        for (ControlRow r : rows) {
            if (r.pass()) passed++;
            System.out.printf(Locale.US, "  %-18s %-13s %8.2f %8s %6s%n",
                    r.sample(), r.category(), r.toolScore(),
                    (r.category().equals(IDEAL) ? ">" : "<") + (int) r.threshold(),
                    r.pass() ? "✓" : "✗");
        }
        System.out.println("-".repeat(60));
        System.out.printf("  %d/%d exemplos dentro da faixa esperada%n", passed, rows.size());
        System.out.println("=".repeat(60));
    }

    static void writeCsv(Path out, List<ControlRow> rows) throws IOException {
        StringBuilder sb = new StringBuilder("sample,category,toolScore,threshold,pass\n");
        for (ControlRow r : rows) {
            sb.append(String.format(Locale.US, "%s,%s,%.2f,%.1f,%b%n",
                    r.sample(), r.category(), r.toolScore(), r.threshold(), r.pass()));
        }
        Files.writeString(out, sb.toString());
    }
}
