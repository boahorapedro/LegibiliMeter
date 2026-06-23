class Snippet34 {
    static int nestedLogic(int a, int b, int c) {
        if (a > 0) {
            if (b > 0) {
                if (c > 0) {
                    if (a < 100) {
                        if (b < 100) {
                            if (c < 100) {
                                return a + b + c;
                            }
                        }
                    }
                }
            }
        }
        return 0;
    }
}