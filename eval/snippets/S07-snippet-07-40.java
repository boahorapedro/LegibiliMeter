class Snippet07 {
    static int countLongWords(String[] words) {
        int count = 0;

        for (String word : words) {

            if (word.length() > 7) {

                count++;
            }
        }

        return count;
    }
}
