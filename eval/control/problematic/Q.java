class Q {
    static long m(int a, int b, int c, int d, int e, int f, int g) {
        long x = 0;
        for (int i = 0; i < a; i++) {
            if (i > b) {
                if ((c ^ d) > (e & f) && (g | a) < (b << 2)) {
                    x = x + ((long)(b * 31) ^ (c << 4) ^ (d >>> 2) | (e & 0xff)) - (f * g) + (a % (b == 0 ? 1 : b)) - (c | d) + (e ^ f);
                }
            }
        }
        return x;
    }
}
