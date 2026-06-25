class Snippet18 {
    static int score(int rawA, int rawB) {
        int a = rawA;
        if (a < 0) {
            a = 0;
        }
        if (a > 100) {
            a = 100;
        }
        int b = rawB;
        if (b < 0) {
            b = 0;
        }
        if (b > 100) {
            b = 100;
        }
        int result = (a + b) / 2;
        return result;
    }
}
