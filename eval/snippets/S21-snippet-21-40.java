class Snippet21 {
    static int calculateInvoiceTotalCents(int itemPriceCents, int shippingCents, int discountCents) {
        int subtotalCents = itemPriceCents + shippingCents;
        int totalCents = subtotalCents - discountCents;
        return Math.max(0, totalCents);
    }
}
