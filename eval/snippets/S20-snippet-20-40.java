import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class Snippet20 {
    int t(int n) {
        return n + (n * 10 / 100);
    }

    @Test
    void x() {
        assertEquals(220, t(200));
        assertEquals(110, t(100));
        assertEquals(0, t(0));
    }
}