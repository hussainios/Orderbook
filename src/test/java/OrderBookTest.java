import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List; 
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
        assertEquals(1, orderBook.getBuyRows().get(0).getId());
        assertEquals(100, orderBook.getBuyRows().get(0).getVolume());
        assertEquals(200, orderBook.getBuyRows().get(0).getPrice());
    }

    @Test
    public void testAddSellOrder() {
        OrderBook orderBook = new OrderBook();
        Order order = Order.limit('S', 1, 200, 100);
        orderBook.addOrder(order);
        assertEquals(0, orderBook.getBuyOrderCount(200));
        assertEquals(1, orderBook.getSellOrderCount(200));
        assertEquals(1, orderBook.getSellRows().get(0).getId());
    }

    @Test
    public void testAddtoNonEmptyBook() {
        OrderBook orderBook = new OrderBook();
        Order order = Order.limit('B', 1, 200, 100);
        Order order1 = Order.limit('B', 2, 200, 100);
        orderBook.addOrder(order);
        orderBook.addOrder(order1);
        assertEquals(2, orderBook.getBuyOrderCount(200));
        assertEquals(1, orderBook.getBuyRows().get(0).getId());
        assertEquals(2, orderBook.getBuyRows().get(1).getId());
    }


    @Test
    public void testCreateBookRows() {
        OrderBook orderBook = new OrderBook();
        
        // Add some buy orders
        Order order1 = Order.limit('B', 1, 200, 100);
        Order order2 = Order.limit('B', 2, 200, 50); 
        Order order3 = Order.limit('B', 3, 190, 75); 

        orderBook.addOrder(order1);
        orderBook.addOrder(order2);
        orderBook.addOrder(order3);

        List<BookRow> buyRows = orderBook.getBuyRows();

        assertEquals(3, buyRows.size());

        assertEquals(1, buyRows.get(0).getId());
        assertEquals(100, buyRows.get(0).getVolume());
        assertEquals(200, buyRows.get(0).getPrice());

        assertEquals(2, buyRows.get(1).getId());
        assertEquals(50, buyRows.get(1).getVolume());
        assertEquals(200, buyRows.get(1).getPrice());

        assertEquals(3, buyRows.get(2).getId());
        assertEquals(75, buyRows.get(2).getVolume());
        assertEquals(190, buyRows.get(2).getPrice());
    }

    @Test
    public void testProcessMatches() {
        OrderBook orderBook = new OrderBook();
        Order order = Order.limit('B', 1, 200, 100);
        Order order1 = Order.limit('S', 2, 200, 100);
        orderBook.addOrder(order);
        orderBook.addOrder(order1);
        assertEquals(0, orderBook.getBuyOrderCount(200));
        assertEquals(0, orderBook.getSellOrderCount(200));
    }

    @Test
    public void testBuyOrderSweepsMultipleSells() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.limit('S', 1, 100, 50));
        orderBook.addOrder(Order.limit('S', 2, 100, 30));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 3, 100, 100));
        assertEquals(2, trades.size());
        assertEquals(50, trades.get(0).getQuantity());
        assertEquals(30, trades.get(1).getQuantity());
        assertEquals(0, orderBook.getSellOrderCount(100));
        assertEquals(1, orderBook.getBuyOrderCount(100)); 
        assertEquals(20, orderBook.getBuyRows().get(0).getVolume());
    }

    @Test
    public void testSellOrderSweepsMultipleBuys() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.limit('B', 1, 100, 40));
        orderBook.addOrder(Order.limit('B', 2, 100, 40));
        List<Trade> trades = orderBook.addOrder(Order.limit('S', 3, 100, 100));
        assertEquals(2, trades.size());
        assertEquals(0, orderBook.getBuyOrderCount(100));
        assertEquals(1, orderBook.getSellOrderCount(100));
    }


    @Test
    public void testLimitMatchesIcebergAcrossPeaks() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 30000, 10000));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 2, 100, 16000));
        assertEquals(1, trades.size());
        assertEquals(16000, trades.get(0).getQuantity());
        assertEquals(1, orderBook.getSellOrderCount(100));
        assertEquals(4000, orderBook.getSellRows().get(0).getVolume());
    }

    @Test
    public void testIcebergMatchesLimit() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.limit('S', 1, 100, 50));
        List<Trade> trades = orderBook.addOrder(Order.iceberg('B', 2, 100, 100, 30));
        assertEquals(1, trades.size());
        assertEquals(50, trades.get(0).getQuantity());
        assertEquals(0, orderBook.getSellOrderCount(100));
        assertEquals(1, orderBook.getBuyOrderCount(100));
    }

    @Test
    public void testIcebergMatchesIceberg() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 50, 20));
        List<Trade> trades = orderBook.addOrder(Order.iceberg('B', 2, 100, 100, 30));
        assertEquals(1, trades.size());
        assertEquals(50, trades.get(0).getQuantity());
        assertEquals(0, orderBook.getSellOrderCount(100));
        assertEquals(1, orderBook.getBuyOrderCount(100));
    }

    @Test
    public void testIcebergReplenishGoesToBackOfQueue() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 30, 10));
        orderBook.addOrder(Order.limit('S', 2, 100, 15));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 3, 100, 10));
        assertEquals(1, trades.size());
        assertEquals(1, trades.get(0).getSellOrderId());
        List<BookRow> sells = orderBook.getSellRows();
        assertEquals(2, sells.size());
        assertEquals(2, sells.get(0).getId());
        assertEquals(1, sells.get(1).getId());
    }

    @Test
    public void testNoMatchWhenPricesDontCross() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.limit('S', 1, 105, 100));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 2, 100, 100));
        assertEquals(0, trades.size());
        assertEquals(1, orderBook.getBuyOrderCount(100));
        assertEquals(1, orderBook.getSellOrderCount(105));
    }

    @Test
    public void testPartialFill() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.limit('S', 1, 100, 30));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 2, 100, 100));
        assertEquals(1, trades.size());
        assertEquals(30, trades.get(0).getQuantity());
        assertEquals(1, orderBook.getBuyOrderCount(100));
        assertEquals(70, orderBook.getBuyRows().get(0).getVolume());
    }

    @Test
    public void testTradeAtExistingOrderPrice() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.limit('S', 1, 100, 50));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 2, 110, 50));
        assertEquals(1, trades.size());
        assertEquals(100, trades.get(0).getPrice());
    }

    @Test
    public void testSweepAcrossMultiplePriceLevels() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.limit('S', 1, 100, 20));
        orderBook.addOrder(Order.limit('S', 2, 101, 30));
        orderBook.addOrder(Order.limit('S', 3, 102, 50));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 4, 102, 100));
        assertEquals(3, trades.size());
        assertEquals(100, trades.get(0).getPrice());
        assertEquals(101, trades.get(1).getPrice());
        assertEquals(102, trades.get(2).getPrice());
        assertEquals(0, orderBook.getSellOrderCount(100));
        assertEquals(0, orderBook.getSellOrderCount(101));
        assertEquals(0, orderBook.getSellOrderCount(102));
    }


    @Test
    public void testIcebergFullyConsumed() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 25, 10));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 2, 100, 50));
        assertEquals(1, trades.size());
        assertEquals(25, trades.get(0).getQuantity());
        assertEquals(0, orderBook.getSellOrderCount(100));
        assertEquals(1, orderBook.getBuyOrderCount(100));
    }

    @Test
    public void testMultipleIcebergsAtSamePrice() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 25, 10));
        orderBook.addOrder(Order.iceberg('S', 2, 100, 25, 10));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 3, 100, 15));
        assertEquals(2, trades.size());
        assertEquals(1, trades.get(0).getSellOrderId());
        assertEquals(2, trades.get(1).getSellOrderId());
    }


    @Test
    public void testIcebergMovesToBackOfQueue() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 30, 10));
        orderBook.addOrder(Order.limit('S', 2, 100, 15));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 3, 100, 10));
        assertEquals(1, trades.size());
        assertEquals(2, orderBook.getSellRows().get(0).getId());
        assertEquals(1, orderBook.getSellRows().get(1).getId());
    }

    @Test
    public void testIcebergWithPeakSizeEqualToTotalQuantity() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 30, 30));
        List<Trade> trades = orderBook.addOrder(Order.limit('B', 2, 100, 30));
        assertEquals(1, trades.size());
        assertEquals(0, orderBook.getSellRows().size());
    }

    @Test
    public void testBothIcebergsReplenishAndContinueMatching() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 20, 10));
        List<Trade> trades = orderBook.addOrder(Order.iceberg('B', 2, 100, 30, 10));
        
        assertEquals(1, trades.size());
        assertEquals(20, trades.get(0).getQuantity());
        assertEquals(0, orderBook.getSellRows().size());
        assertEquals(1, orderBook.getBuyRows().size());
        assertEquals(10, orderBook.getBuyRows().get(0).getVolume());
    }

    @Test
    public void testMultipleIcebergsInQueueReplenishment() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 10, 5));
        orderBook.addOrder(Order.iceberg('S', 2, 100, 15, 5));
        List<Trade> trades = orderBook.addOrder(Order.iceberg('B', 3, 100, 25, 5));
        
        int totalQuantity = trades.stream().mapToInt(Trade::getQuantity).sum();
        assertEquals(25, totalQuantity);
        assertEquals(0, orderBook.getSellRows().size());
        assertEquals(0, orderBook.getBuyRows().size());
    }


    @Test
    public void testIcebergMatchesMultipleSellOrders() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.limit('S', 1, 10, 10));
        orderBook.addOrder(Order.limit('S', 2, 10, 11));
        List<Trade> trades = orderBook.addOrder(Order.iceberg('B', 3, 10, 100, 10));
        
        int totalMatched = trades.stream().mapToInt(Trade::getQuantity).sum();
        assertEquals(21, totalMatched);
        assertEquals(0, orderBook.getSellRows().size());
        assertEquals(1, orderBook.getBuyRows().size());
        assertEquals(9, orderBook.getBuyRows().get(0).getVolume());
        assertEquals(2, trades.size());
    }

    @Test
    public void testIncomingMatchesIcebergThenNextOrder() {
    OrderBook orderBook = new OrderBook();
    orderBook.addOrder(Order.iceberg('S', 1, 100, 30, 10));
    orderBook.addOrder(Order.limit('S', 2, 100, 15));
    List<Trade> trades = orderBook.addOrder(Order.limit('B', 3, 100, 25));
    
    assertEquals(2, trades.size());
    assertEquals(10, trades.get(0).getQuantity()); 
    assertEquals(15, trades.get(1).getQuantity()); 
    assertEquals(1, orderBook.getSellOrderCount(100)); 
    assertEquals(10, orderBook.getSellRows().get(0).getVolume()); 
}

    @Test
    public void testIcebergRestingVisibleQuantityWhenNoMatch() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('B', 10, 200, 100, 15));
        assertEquals(1, orderBook.getBuyOrderCount(200));
        assertEquals(15, orderBook.getBuyRows().get(0).getVolume());
        assertEquals(10, orderBook.getBuyRows().get(0).getId());
    }

    @Test
    public void testIcebergRequeueBehindAllSamePriceOrders() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.iceberg('S', 1, 100, 10, 5));
        orderBook.addOrder(Order.limit('S', 2, 100, 5));
        orderBook.addOrder(Order.limit('S', 3, 100, 5));
        orderBook.addOrder(Order.limit('B', 4, 100, 5));
        List<BookRow> sells = orderBook.getSellRows();
        assertEquals(3, sells.size());
        assertEquals(2, sells.get(0).getId());
        assertEquals(3, sells.get(1).getId());
        assertEquals(1, sells.get(2).getId());
    }

    @Test
    public void testFIFOWithinSamePriceLevel() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(Order.limit('S', 1, 100, 5));
        orderBook.addOrder(Order.limit('S', 2, 100, 7));

        List<Trade> trades = orderBook.addOrder(Order.limit('B', 3, 100, 10));
        assertEquals(2, trades.size());
        assertEquals(1, trades.get(0).getSellOrderId());
        assertEquals(2, trades.get(1).getSellOrderId());
        assertEquals(5, trades.get(0).getQuantity());
        assertEquals(5, trades.get(1).getQuantity());
        assertEquals(1, orderBook.getSellOrderCount(100));
        assertEquals(2, orderBook.getSellRows().get(0).getId());
        assertEquals(2, orderBook.getSellRows().get(0).getVolume());
    }
}
