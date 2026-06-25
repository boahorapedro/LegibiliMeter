class Snippet08 {
    static int countLongWords(String[] words) {
        int c = 0;
        for (String w : words) {
            if (w.length() > 7) {
                c++;
            }
        }
        return c;
    }
}