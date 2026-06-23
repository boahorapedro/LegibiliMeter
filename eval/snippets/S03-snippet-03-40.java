class Snippet03 {
    static double calculateAverageMonthlySubscriptionRevenueInUsd(double[] monthlyRevenueValuesInUsd) {
        if (monthlyRevenueValuesInUsd.length == 0) {
            return 0.0;
        }

        double accumulatedMonthlyRevenueInUsd = 0.0;
        for (double individualMonthRevenueInUsd : monthlyRevenueValuesInUsd) {
            accumulatedMonthlyRevenueInUsd += individualMonthRevenueInUsd;
        }

        return accumulatedMonthlyRevenueInUsd / monthlyRevenueValuesInUsd.length;
    }
}
