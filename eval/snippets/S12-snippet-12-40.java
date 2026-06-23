class Snippet12 {
    static String mix(int a, int b, int c) {
        int v1 = (a * 31) ^ (b << 2) ^ (c >>> 1);
        int v2 = ((a + 7) * (b - 3)) % (c == 0 ? 1 : c);
        long v3 = ((long) v1 << 32) | (v2 & 0xffffffffL);
        return Long.toHexString(v3) + ":" + Integer.toOctalString(v1 ^ v2);
    }
}
