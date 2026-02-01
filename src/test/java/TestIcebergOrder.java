import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestIcebergOrder {

    @Test
    public void testInitializationUsesPeakSize() {
        IcebergOrder order = new IcebergOrder('B', 1, 200, 100, 20);
        assertEquals('B', order.getSide());
        assertEquals(1, order.getId());
        assertEquals(200, order.getPrice());
        assertEquals(100, order.getTotalQuantity());
        assertEquals(20, order.getPeakSize());
        assertEquals(20, order.getVisibleQuantity());
    }

    @Test
    public void testInitializationWhenPeakExceedsTotal() {
        IcebergOrder order = new IcebergOrder('B', 1, 200, 15, 20);
        assertEquals(15, order.getTotalQuantity());
        assertEquals(15, order.getVisibleQuantity());
    }

    @Test
    public void testReduceQuantityClampsVisibleToZero() {
        IcebergOrder order = new IcebergOrder('B', 1, 200, 50, 20);
        order.reduceQuantity(25);
        assertEquals(25, order.getTotalQuantity());
        assertEquals(0, order.getVisibleQuantity());
    }

    @Test
    public void testReplenishWithRemainingTotal() {
        IcebergOrder order = new IcebergOrder('B', 1, 200, 100, 20);
        order.reduceQuantity(20);
        assertEquals(80, order.getTotalQuantity());
        assertEquals(0, order.getVisibleQuantity());

        order.replenish();
        assertEquals(80, order.getTotalQuantity());
        assertEquals(20, order.getVisibleQuantity());
    }

    @Test
    public void testReplenishWhenTotalIsZero() {
        IcebergOrder order = new IcebergOrder('B', 1, 200, 50, 20);
        order.reduceQuantity(50);
        assertEquals(0, order.getTotalQuantity());
        assertEquals(0, order.getVisibleQuantity());

        order.replenish();
        assertEquals(0, order.getTotalQuantity());
        assertEquals(0, order.getVisibleQuantity());
    }
}
