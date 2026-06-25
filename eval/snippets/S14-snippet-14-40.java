class Snippet14 {
    static int compute(int a, int b, int c, int d) {
        int r1 = (a + b) * (c - d);
        int r2 = (a ^ c) & (b | d);
        int r3 = ((a << 1) + (b >> 1)) - (c % (d == 0 ? 1 : d));
        int r4 = (r1 > r2 ? r1 : r2) + (r3 < 0 ? -r3 : r3);
        return ((r4 ^ r2) + (r1 & r3)) / ((a & 1) + 1);
    }
}
