class Calc {
    static int d(int a, int b, int c, int d, int e, int f) {
        int r = 0;
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                if (i > c) {
                    if (j < d) {
                        if (e > f) {
                            r = r + i * j - c + d * e % (f == 0 ? 1 : f) + (a ^ b) - (c & d);
                        }
                    }
                }
            }
        }
        return r;
    }
}
