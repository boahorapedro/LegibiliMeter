package br.ufpe.cin.metric.scorer;

/**
 * Resultado normalizado de uma única feature: valor bruto e score [0–10].
 */
public record FeatureScore(String feature, double rawValue, double score) {}