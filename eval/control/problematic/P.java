class P {
    static int g(int a, int b, int c, int d, int e, int h) {
        int r = 0;
        for (int i = 0; i < a; i++) {
            if (i > b) {
                while (c > 0) {
                    if (d < e) {
                        if (h > 0) {
                            r = r + i;
                        }
                    }
                    c = c - 1;
                }
            }
        }
        return r;
    }
}
