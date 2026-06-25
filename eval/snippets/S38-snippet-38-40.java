class Snippet38 {
    static int categorizeAndCount(int[] values) {
        int positive = 0, negative = 0, zero = 0;
        for (int v : values) {
            if (v > 0) {
                positive++;
            } else if (v < 0) {
                negative++;
            } else {
                zero++;
            }
        }
        int result = 0;
        while (positive > 0) {
            result += positive;
            positive--;
        }
        for (int i = 0; i < negative; i++) {
            result -= i;
        }
        return result + zero;
    }
}