package br.ufpe.cin.metric;

import br.ufpe.cin.metric.extractor.FeatureExtractor;
import br.ufpe.cin.metric.model.FeatureResult;
import br.ufpe.cin.metric.model.SourceFile;
import br.ufpe.cin.metric.parser.JavaFileLoader;
import br.ufpe.cin.metric.reporter.Reporter;
import br.ufpe.cin.metric.scorer.ScoreResult;
import br.ufpe.cin.metric.scorer.Scorer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Ponto de entrada da CLI.
 *
 * Pipeline: arquivo.java → Parser → FeatureExtractor → Scorer → Reporter → nota [0–10]
 *
 * Uso:
 *   java Main &lt;arquivo.java&gt; [--json &lt;saida.json&gt;] [--csv &lt;saida.csv&gt;]
 */
public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out.println("Uso: java Main <arquivo.java> [--json <saida.json>] [--csv <saida.csv>]");
            return;
        }

        String inputPath = args[0];
        String jsonPath = optionValue(args, "--json");
        String csvPath = optionValue(args, "--csv");

        // 1. Parser & Loader
        SourceFile source = JavaFileLoader.loadSource(inputPath);

        // 2. Feature Extractor
        Map<String, FeatureResult> rawResults = new FeatureExtractor().extractAll(source);

        // 3. Scorer
        ScoreResult result = new Scorer().score(rawResults);

        // 4. Reporter
        Reporter reporter = new Reporter();
        System.out.print(reporter.toTerminal(source.name(), result));

        if (jsonPath != null) {
            Files.writeString(Path.of(jsonPath), reporter.toJson(source.name(), result));
            System.out.println("Relatório JSON gerado em: " + jsonPath);
        }
        if (csvPath != null) {
            Files.writeString(Path.of(csvPath), reporter.toCsv(source.name(), result));
            System.out.println("Relatório CSV gerado em: " + csvPath);
        }
    }

    /** Lê o valor que segue uma flag (ex.: {@code --json saida.json}); null se ausente. */
    private static String optionValue(String[] args, String flag) {
        for (int i = 0; i < args.length - 1; i++) {
            if (args[i].equals(flag)) {
                return args[i + 1];
            }
        }
        return null;
    }
}
