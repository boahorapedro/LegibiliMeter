class Snippet31 {
    static String processAndFormatAndDisplayAndReturnVeryLongResultStringFromMultipleSourcesAndCombineThemTogether(String firstInputData, String secondInputData, String thirdInputData) {
        String firstProcessedData = firstInputData.toUpperCase() + "_" + secondInputData.toLowerCase() + "_" + thirdInputData.replace(" ", "_");
        String secondProcessedData = firstProcessedData.substring(0, Math.min(firstProcessedData.length(), 100)).trim();
        return "RESULT:" + secondProcessedData + ":" + String.valueOf(System.currentTimeMillis());
    }
}