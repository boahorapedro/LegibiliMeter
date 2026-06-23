class Snippet06 {
    static String classify(int age, boolean hasTicket, boolean vip, boolean blocked) {
        if (blocked) {
            return "blocked";
        }
        if (age < 18 && !hasTicket) {
            return "minor-no-ticket";
        }
        if (age < 18) {
            return "minor-with-ticket";
        }
        if (!hasTicket) {
            return "adult-no-ticket";
        }
        if (vip) {
            return "adult-vip";
        }
        return "adult-regular";
    }
}
