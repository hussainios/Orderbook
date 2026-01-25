import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestParse {

    @Test
    public void testParseLimitOrder() {
        Order order = InputParser.parseLine("B,100322,5103,7500");
        
        assertNotNull(order);
        assertEquals('B', order.getSide());
        assertEquals(100322, order.getId());
        assertEquals(5103, order.getPrice());
        assertEquals(7500, order.getTotalQuantity());
        assertEquals(0, order.getPeakSize());
        assertEquals(7500, order.getVisibleQuantity()); // Limit order: fully visible
    }

    @Test
    public void testParseIcebergOrder() {
        Order order = InputParser.parseLine("S,100345,5103,100000,10000");
        
        assertNotNull(order);
        assertEquals('S', order.getSide());
        assertEquals(100345, order.getId());
        assertEquals(5103, order.getPrice());
        assertEquals(100000, order.getTotalQuantity());
        assertEquals(10000, order.getPeakSize());
        assertEquals(10000, order.getVisibleQuantity()); // Iceberg: min(total, peak)
    }

    @Test
    public void testParseEmptyLine() {
        assertNull(InputParser.parseLine(""));
        assertNull(InputParser.parseLine("   "));
    }

    @Test
    public void testParseComment() {
        assertNull(InputParser.parseLine("# This is a comment"));
        assertNull(InputParser.parseLine(" # Another comment"));
    }

    @Test
    public void testParseInvalidSide() {
        assertNull(InputParser.parseLine("X,1,100,50"));
    }

    @Test
    public void testParseInvalidFormat() {
        assertNull(InputParser.parseLine("B,1,100")); // Too few fields
        assertNull(InputParser.parseLine("B,1,100,50,10,20")); // Too many fields
    }
}