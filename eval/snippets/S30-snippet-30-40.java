class Snippet30 {
    static int run(int[] data) {
        int field = 0;
        for (int value : data) {
            if (value > 0) {
                field += value;
            }
        }
        return field;
    }
}
