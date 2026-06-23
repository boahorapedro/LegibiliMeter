class NumberSummer {
    static int sumPositiveValues(int[] numbers) {
        int runningTotal = 0;
        for (int currentNumber : numbers) {
            if (currentNumber > 0) {
                runningTotal = runningTotal + currentNumber;
            }
        }
        return runningTotal;
    }
}
