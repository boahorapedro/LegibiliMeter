class Snippet05 {
    static String classify(int age, boolean hasTicket, boolean vip, boolean blocked) {
        if (!blocked) {
            if (age >= 18) {
                if (hasTicket) {
                    if (vip) {
                        return "adult-vip";
                    } else {
                        return "adult-regular";
                    }
                } else {
                    return "adult-no-ticket";
                }
            } else {
                if (hasTicket) {
                    return "minor-with-ticket";
                } else {
                    return "minor-no-ticket";
                }
            }
        }
        return "blocked";
    }
}
