import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class TestTrade {
    @Test
    public void testTradeToString() {
        Trade trade = new Trade(1, 2, 100, 50);
        assertEquals("1,2,100,50", trade.toString());
    }
}