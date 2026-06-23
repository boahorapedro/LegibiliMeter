class Snippet16 {
    static String process(String[] words, int[] values) {
        int sum = 0;
        for (int value : values) {
            sum += value;
        }

        int longWords = 0;
        for (String word : words) {
            if (word.length() > 5) {
                longWords++;
            }
        }

        String status = sum > 100 ? "high" : "low";
        return status + "-" + longWords + "-" + Integer.toHexString(sum);
    }
}
