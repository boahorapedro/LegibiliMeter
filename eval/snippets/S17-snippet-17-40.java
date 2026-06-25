class Snippet17 {
    static int normalize(int value) {
        return Math.max(0, Math.min(100, value));
    }

    static int average(int a, int b) {
        return (a + b) / 2;
    }

    static int score(int rawA, int rawB) {
        int a = normalize(rawA);
        int b = normalize(rawB);
        return average(a, b);
    }
}
