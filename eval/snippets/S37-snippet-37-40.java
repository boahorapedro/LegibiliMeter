class Snippet37 {
    static int complexCalculation(int a, int b, int c, int d) {
        int result = ((a + b) * c) - d;
        result = result / (d != 0 ? d : 1);
        result = (a & b) | (c ^ d);
        result = result << 2;
        result = result >> 1;
        int final_result = (result > 0) ? result : -result;
        return (final_result >= 100) ? 100 : ((final_result <= 0) ? 0 : final_result);
    }
}