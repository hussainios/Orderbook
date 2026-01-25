import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderBookTest {

    @Test
    public void testAddSimpleBuyOrder() {
        OrderBook book = new OrderBook();
        Order buyOrder = Order.limit('B', 1, 100, 50);
        
        book.addOrder(buyOrder);
        
        // Verify the order is resting on the buy side at price 100
        assertEquals(1, book.getBuyOrderCount(100), "Should have 1 order at price 100");
    }

    @Test
    public void testAddSimpleSellOrder() {
        OrderBook book = new OrderBook();
        Order sellOrder = Order.limit('S', 2, 105, 75);
        
        book.addOrder(sellOrder);
        
        // Verify the order is resting on the sell side at price 105
        assertEquals(1, book.getSellOrderCount(105), "Should have 1 order at price 105");
    }

    @Test
    public void testMultipleOrdersAtSamePrice() {
        OrderBook book = new OrderBook();
        book.addOrder(Order.limit('B', 1, 100, 50));
        book.addOrder(Order.limit('B', 2, 100, 30));
        
        assertEquals(2, book.getBuyOrderCount(100), "Should have 2 orders at price 100");
    }

    @Test
    public void testBasicMatch() {
        OrderBook book = new OrderBook();
        // Resting sell
        book.addOrder(Order.limit('S', 1, 100, 50));
        // Incoming buy
        book.addOrder(Order.limit('B', 2, 100, 50));

        assertEquals(0, book.getSellOrderCount(100), "Sell order should be gone");
        assertEquals(0, book.getBuyOrderCount(100), "Buy order should be gone");
    }

    @Test
    public void testIcebergReplenishment() {
        OrderBook book = new OrderBook();
        // Resting iceberg buy: Total 100, Peak 40, Price 100
        book.addOrder(Order.iceberg('B', 1, 100, 100, 40));
        
        // Incoming sell: Qty 50, Price 100
        book.addOrder(Order.limit('S', 2, 100, 50));

        // After matching 40 from the first peak, the iceberg should replenish
        // with 40 more, and then match the remaining 10.
        // Total matched: 50. Remaining iceberg total: 50. Visible: 40.
        assertEquals(1, book.getBuyOrderCount(100));
        // We can't easily check internal state without more getters, 
        // but the fact that it's still there is a start.
    }
}

