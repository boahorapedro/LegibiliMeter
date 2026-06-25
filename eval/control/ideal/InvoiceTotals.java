class InvoiceTotals {
    static double calculateTotal(double subtotal, double discount) {
        double netAmount = subtotal - discount;
        return netAmount;
    }
}
