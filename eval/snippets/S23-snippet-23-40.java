class Snippet23 {
    static boolean isStrongPassword(String password) {
        boolean longEnough = password.length() >= 8;
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasDigit = password.matches(".*\\d.*");
        return longEnough && hasUpper && hasDigit;
    }
}
