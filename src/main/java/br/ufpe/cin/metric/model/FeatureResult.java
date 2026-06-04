package br.ufpe.cin.metric.model;

public record FeatureResult(String feature, double max, double mean) {

    public static FeatureResult single(String feature, double value) {
        return new FeatureResult(feature, value, value);
    }
}
