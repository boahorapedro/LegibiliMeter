import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class Snippet19 {
    int addTax(int net) {
        return net + (net * 10 / 100);
    }

    @Test
    void addTax_shouldApplyTenPercent() {
        int netPrice = 200;

        int finalPrice = addTax(netPrice);

        assertEquals(220, finalPrice);
    }
}
