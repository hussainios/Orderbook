import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestInputParser {

    @Test
    public void testParseLimitOrder() {
        BookCommand command = InputParser.parseLine("ADD,B,100322,5103,7500");
        
        assertNotNull(command);
        assertTrue(command instanceof AddCommand);
        Order order = ((AddCommand) command).getOrder();
        assertTrue(order instanceof LimitOrder);
        assertEquals('B', order.getSide());
        assertEquals(100322, order.getId());
        assertEquals(5103, order.getPrice());
        assertEquals(7500, order.getTotalQuantity());
        // LimitOrder does not have peak size
        assertEquals(7500, order.getVisibleQuantity()); 
    }

    @Test
    public void testParseIcebergOrder() {
        BookCommand command = InputParser.parseLine("ADD,S,100345,5103,100000,10000");
        
        assertNotNull(command);
        assertTrue(command instanceof AddCommand);
        Order order = ((AddCommand) command).getOrder();
        assertTrue(order instanceof IcebergOrder);
        assertEquals('S', order.getSide());
        assertEquals(100345, order.getId());
        assertEquals(5103, order.getPrice());
        assertEquals(100000, order.getTotalQuantity());
        assertEquals(10000, ((IcebergOrder)order).getPeakSize());
        assertEquals(10000, order.getVisibleQuantity()); 
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
    public void testParseCancelCommand() {
        BookCommand command = InputParser.parseLine("CANCEL,100322");
        assertNotNull(command);
        assertTrue(command instanceof CancelCommand);
        assertEquals(100322, ((CancelCommand) command).getOrderId());
    }

    @Test
    public void testParseInvalidSide() {
        assertNull(InputParser.parseLine("ADD,X,1,100,50"));
    }

    @Test
    public void testParseInvalidFormat() {
        assertNull(InputParser.parseLine("ADD,B,1,100"));
        assertNull(InputParser.parseLine("ADD,B,1,100,50,10,20"));
        assertNull(InputParser.parseLine("CANCEL"));
        assertNull(InputParser.parseLine("CANCEL,1,2"));
    }

    @Test
    public void testRejectLegacyOrderSyntax() {
        assertNull(InputParser.parseLine("B,1,100,50"));
    }
}
