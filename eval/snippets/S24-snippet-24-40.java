class Snippet24 {
    static boolean isStrongPassword(String p) {
        return p.length() >= 8 &&
               !p.equals(p.toLowerCase()) &&
               p.matches(".*\\d.*") &&
               p.matches(".*[!@#$%^&*()_+=\\-\\[\\]{};':"\\|,.<>/?].*");
    }
}