import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestOrder {

    @Test
    public void testInitialisation() {
        // Testing LimitOrder initialization
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
    public void testReduceQuantityToZero() {
        LimitOrder order = new LimitOrder('B', 1, 200, 100);
        order.reduceQuantity(100);
        assertEquals(0, order.getTotalQuantity());
        assertEquals(0, order.getVisibleQuantity());
    }

    @Test 
    public void testReplenish() {
        IcebergOrder order = new IcebergOrder('B', 1, 200, 100, 100);
        // Reduce to 0 visible
        order.reduceQuantity(100);
        // Since peak is 100, replenish should theoretically fill it back if total > 0.
        // But here total became 0.
        assertEquals(0, order.getTotalQuantity());
        
        // Let's test actual replenishment scenario
        IcebergOrder order2 = new IcebergOrder('B', 2, 200, 100, 20);
        // reduce 20 (visible becomes 0)
        order2.reduceQuantity(20); 
        assertEquals(80, order2.getTotalQuantity());
        assertEquals(0, order2.getVisibleQuantity());
        
        order2.replenish();
        assertEquals(80, order2.getTotalQuantity());
        assertEquals(20, order2.getVisibleQuantity());
    }

    @Test 
    public void testLimitOrder() {
        LimitOrder order = new LimitOrder('B', 1, 200, 100);
        assertEquals('B', order.getSide());
        assertEquals(1, order.getId());
        assertEquals(200, order.getPrice());
        assertEquals(100, order.getTotalQuantity());
        assertEquals(100, order.getVisibleQuantity());
    }

    @Test
    public void testIcebergOrder() {
        IcebergOrder order = new IcebergOrder('B', 1, 200, 100, 100);
        assertEquals('B', order.getSide());
        assertEquals(1, order.getId());
        assertEquals(200, order.getPrice());
        assertEquals(100, order.getTotalQuantity());
        assertEquals(100, order.getPeakSize());
        assertEquals(100, order.getVisibleQuantity());
    }
}
