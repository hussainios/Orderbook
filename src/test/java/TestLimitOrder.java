import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLimitOrder {

    @Test
    public void testInitialization() {
        LimitOrder order = new LimitOrder('B', 1, 200, 100);
        assertEquals('B', order.getSide());
        assertEquals(1, order.getId());
        assertEquals(200, order.getPrice());
        assertEquals(100, order.getTotalQuantity());
        assertEquals(100, order.getVisibleQuantity());
    }

    @Test
    public void testReduceQuantity() {
        LimitOrder order = new LimitOrder('B', 1, 200, 100);
        order.reduceQuantity(50);
        assertEquals(50, order.getTotalQuantity());
        assertEquals(50, order.getVisibleQuantity());
    }

    @Test
    public void testReplenishNoChange() {
        LimitOrder order = new LimitOrder('B', 1, 200, 100);
        order.reduceQuantity(30);
        order.replenish();
        assertEquals(70, order.getTotalQuantity());
        assertEquals(70, order.getVisibleQuantity());
    }
}
