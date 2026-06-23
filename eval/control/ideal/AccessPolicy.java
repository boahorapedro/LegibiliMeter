class AccessPolicy {
    static String classifyUser(int age, boolean hasTicket) {
        if (age < 18) {
            return "minor";
        }
        if (!hasTicket) {
            return "adult-without-ticket";
        }
        return "adult-with-ticket";
    }
}
