class Snippet36 {
    // Main processing function
    static int process(int value) {
        // Initialize result variable
        int result = 0;
        
        // Check if value is positive
        if (value > 0) {
            // Multiply by 2
            result = value * 2;
            // Add 10
            result = result + 10;
        } else if (value < 0) {
            // Make positive
            result = -value;
            // Subtract 5
            result = result - 5;
        } else {
            // Value is zero
            result = 0;
        }
        
        // Return final result
        return result;
    }
}