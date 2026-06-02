package br.ufpe.cin.metric.extractor;

/**
 * Contrato comum às cinco features da métrica de legibilidade.
 *
 * <p>Cada feature analisa um {@link SourceFile} (texto e/ou AST) e produz um
 * {@link FeatureResult} com o(s) valor(es) bruto(s). O Scorer normaliza esses
 * valores e aplica a soma ponderada; o Reporter usa {@link #name()} para rotular
 * o detalhamento.
 *
 * <p>Implementações devem ser sem estado (stateless) para poderem ser reutilizadas
 * entre arquivos.
 */
public interface Feature {

    /** Identificador estável da feature (usado pelo Scorer e pelo Reporter). */
    String name();

    /** Extrai o valor bruto da feature a partir do arquivo. */
    FeatureResult extract(SourceFile file);
}
