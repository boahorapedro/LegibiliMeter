class Snippet09 {
    // Converts cents to a currency string like 10.25.
    static String formatPrice(int cents) {
        // Keep integer math to avoid floating point rounding artifacts.
        int dollars = cents / 100;
        int remainder = Math.abs(cents % 100);

        // Always pad with two decimal digits.
        String decimal = remainder < 10 ? "0" + remainder : String.valueOf(remainder);
        return dollars + "." + decimal;
    }
}
