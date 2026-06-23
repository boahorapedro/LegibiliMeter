class Snippet02 {
    static int f(int[] a) {
        int s = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > 0) {
                s = s + a[i];
            }
        }
        return s;
    }
}