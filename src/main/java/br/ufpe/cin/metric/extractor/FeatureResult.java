package br.ufpe.cin.metric.extractor;

/**
 * Valor bruto de uma feature para um arquivo, antes da normalização do Scorer.
 *
 * <p>Para features por-método (parâmetros, aninhamento, complexidade cognitiva),
 * {@code max} é o valor do pior método e {@code mean} a média entre todos os métodos.
 * A escolha de qual usar no score é decidida na fase de calibração (Semana 4).
 *
 * <p>Para features por-arquivo (comprimento de linha, comprimento de identificadores),
 * não há granularidade de método: use {@link #single(String, double)}, que faz
 * {@code max == mean == valor}.
 */
public record FeatureResult(String feature, double max, double mean) {

    public static FeatureResult single(String feature, double value) {
        return new FeatureResult(feature, value, value);
    }
}
