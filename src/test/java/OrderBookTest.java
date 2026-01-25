import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class OrderBookTest {
    @Test
    public void testAddLimitOrder() {
        OrderBook orderBook = new OrderBook();
        Order order = Order.limit('B', 1, 200, 100);
        orderBook.addOrder(order);
        assertEquals(1, orderBook.getBuyOrderCount(200));
        assertEquals(0, orderBook.getSellOrderCount(200));
    }

    @Test
    public void testAddIcebergOrder() {
        OrderBook orderBook = new OrderBook();
        Order order = Order.iceberg('B', 1, 200, 100, 100);
        orderBook.addOrder(order);
        assertEquals(1, orderBook.getBuyOrderCount(200));
        assertEquals(0, orderBook.getSellOrderCount(200));
    }

    @Test
    public void testAddSellOrder() {
        OrderBook orderBook = new OrderBook();
        Order order = Order.limit('S', 1, 200, 100);
        orderBook.addOrder(order);
        assertEquals(0, orderBook.getBuyOrderCount(200));
        assertEquals(1, orderBook.getSellOrderCount(200));
    }

    @Test
    public void testAddtoNonEmptyBook() {
        OrderBook orderBook = new OrderBook();
        Order order = Order.limit('B', 1, 200, 100);
        Order order1 = Order.limit('B', 2, 200, 100);
        orderBook.addOrder(order);
        orderBook.addOrder(order1);
        assertEquals(2, orderBook.getBuyOrderCount(200));
    }


    @Test
    public void testCreateBookRows() {
        
    }

}