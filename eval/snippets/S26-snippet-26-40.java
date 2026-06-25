class Snippet26 {
    static int aggregate(int baseValue, int incrementValue, int multiplierValue, int limitValue) {
        int intermediateValue = baseValue + incrementValue;
        int scaledIntermediateValue = intermediateValue * multiplierValue;
        int boundedScaledIntermediateValue = Math.min(scaledIntermediateValue, limitValue);
        return boundedScaledIntermediateValue;
    }
}
