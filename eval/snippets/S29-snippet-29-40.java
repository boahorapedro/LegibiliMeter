class Snippet29 {
    // Finds the first overdue invoice index, or -1 if all invoices are on time.
    static int findFirstOverdue(int[] invoiceDaysLate) {
        for (int index = 0; index < invoiceDaysLate.length; index++) {
            if (invoiceDaysLate[index] > 0) {
                return index;
            }
        }
        return -1;
    }
}
