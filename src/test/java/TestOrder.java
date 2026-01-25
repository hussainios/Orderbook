import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestOrder {

    @Test
    public void testInitialisation() {
        Order order = new Order('B', 1, 200, 100, 0, 100);
        assertEquals('B', order.getSide());
        assertEquals(1, order.getId());
        assertEquals(200, order.getPrice());
        assertEquals(100, order.getTotalQuantity());
        assertEquals(0, order.getPeakSize());
        assertEquals(100, order.getVisibleQuantity());
    }

    @Test
    public void testReduceQuantity() {
        Order order = new Order('B', 1, 200, 100, 0, 100);
        order.reduceQuantity(50);
        assertEquals(50, order.getTotalQuantity());
        assertEquals(50, order.getVisibleQuantity());
    }

    @Test
    public void testReduceQuantityToZero() {
        Order order = new Order('B', 1, 200, 100, 0, 100);
        order.reduceQuantity(100);
        assertEquals(0, order.getTotalQuantity());
        assertEquals(0, order.getVisibleQuantity());
    }

    @Test 
    public void testReplenish() {
        Order order = new Order('B', 1, 200, 100, 100, 0);
        order.replenish();
        assertEquals(100, order.getTotalQuantity());
        assertEquals(100, order.getVisibleQuantity());
    }

    @Test 
    public void testLimitOrder() {
        Order order = Order.limit('B', 1, 200, 100);
        assertEquals('B', order.getSide());
        assertEquals(1, order.getId());
        assertEquals(200, order.getPrice());
        assertEquals(100, order.getTotalQuantity());
        assertEquals(0, order.getPeakSize());
        assertEquals(100, order.getVisibleQuantity());
    }

    @Test
    public void testIcebergOrder() {
        Order order = Order.iceberg('B', 1, 200, 100, 100);
        assertEquals('B', order.getSide());
        assertEquals(1, order.getId());
        assertEquals(200, order.getPrice());
        assertEquals(100, order.getTotalQuantity());
        assertEquals(100, order.getPeakSize());
        assertEquals(100, order.getVisibleQuantity());
    }
}