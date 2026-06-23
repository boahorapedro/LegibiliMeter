class Snippet04 {
    static double g(double[] x) {
        if (x.length == 0) {
            return 0;
        }

        double y = 0;
        for (double z : x) {
            y += z;
        }

        return y / x.length;
    }
}
