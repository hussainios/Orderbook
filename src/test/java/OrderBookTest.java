import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List; 
public class OrderBookTest {
    @Test
    public void testAddLimitOrder() {
        OrderBook orderBook = new OrderBook();
        Order order = new LimitOrder('B', 1, 200, 100);
        orderBook.addOrder(order);
        assertEquals(1, orderBook.getBuyOrderCount(200));
        assertEquals(0, orderBook.getSellOrderCount(200));
    }

    @Test
    public void testAddIcebergOrder() {
        OrderBook orderBook = new OrderBook();
        Order order = new IcebergOrder('B', 1, 200, 100, 100);
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
        Order order = new LimitOrder('S', 1, 200, 100);
        orderBook.addOrder(order);
        assertEquals(0, orderBook.getBuyOrderCount(200));
        assertEquals(1, orderBook.getSellOrderCount(200));
        assertEquals(1, orderBook.getSellRows().get(0).getId());
    }

    @Test
    public void testAddtoNonEmptyBook() {
        OrderBook orderBook = new OrderBook();
        Order order = new LimitOrder('B', 1, 200, 100);
        Order order1 = new LimitOrder('B', 2, 200, 100);
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
        Order order1 = new LimitOrder('B', 1, 200, 100);
        Order order2 = new LimitOrder('B', 2, 200, 50); 
        Order order3 = new LimitOrder('B', 3, 190, 75); 

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
        Order order = new LimitOrder('B', 1, 200, 100);
        Order order1 = new LimitOrder('S', 2, 200, 100);
        orderBook.addOrder(order);
        orderBook.addOrder(order1);
        assertEquals(0, orderBook.getBuyOrderCount(200));
        assertEquals(0, orderBook.getSellOrderCount(200));
    }

    @Test
    public void testBuyOrderSweepsMultipleSells() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new LimitOrder('S', 1, 100, 50));
        orderBook.addOrder(new LimitOrder('S', 2, 100, 30));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 3, 100, 100));
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
        orderBook.addOrder(new LimitOrder('B', 1, 100, 40));
        orderBook.addOrder(new LimitOrder('B', 2, 100, 40));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('S', 3, 100, 100));
        assertEquals(2, trades.size());
        assertEquals(0, orderBook.getBuyOrderCount(100));
        assertEquals(1, orderBook.getSellOrderCount(100));
    }


    @Test
    public void testLimitMatchesIcebergAcrossPeaks() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 30000, 10000));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 2, 100, 16000));
        assertEquals(1, trades.size());
        assertEquals(16000, trades.get(0).getQuantity());
        assertEquals(1, orderBook.getSellOrderCount(100));
        assertEquals(4000, orderBook.getSellRows().get(0).getVolume());
    }

    @Test
    public void testIcebergMatchesLimit() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new LimitOrder('S', 1, 100, 50));
        List<Trade> trades = orderBook.addOrder(new IcebergOrder('B', 2, 100, 100, 30));
        assertEquals(1, trades.size());
        assertEquals(50, trades.get(0).getQuantity());
        assertEquals(0, orderBook.getSellOrderCount(100));
        assertEquals(1, orderBook.getBuyOrderCount(100));
    }

    @Test
    public void testIcebergMatchesIceberg() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 50, 20));
        List<Trade> trades = orderBook.addOrder(new IcebergOrder('B', 2, 100, 100, 30));
        assertEquals(1, trades.size());
        assertEquals(50, trades.get(0).getQuantity());
        assertEquals(0, orderBook.getSellOrderCount(100));
        assertEquals(1, orderBook.getBuyOrderCount(100));
    }

    @Test
    public void testIcebergReplenishGoesToBackOfQueue() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 30, 10));
        orderBook.addOrder(new LimitOrder('S', 2, 100, 15));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 3, 100, 10));
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
        orderBook.addOrder(new LimitOrder('S', 1, 105, 100));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 2, 100, 100));
        assertEquals(0, trades.size());
        assertEquals(1, orderBook.getBuyOrderCount(100));
        assertEquals(1, orderBook.getSellOrderCount(105));
    }

    @Test
    public void testPartialFill() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new LimitOrder('S', 1, 100, 30));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 2, 100, 100));
        assertEquals(1, trades.size());
        assertEquals(30, trades.get(0).getQuantity());
        assertEquals(1, orderBook.getBuyOrderCount(100));
        assertEquals(70, orderBook.getBuyRows().get(0).getVolume());
    }

    @Test
    public void testTradeAtExistingOrderPrice() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new LimitOrder('S', 1, 100, 50));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 2, 110, 50));
        assertEquals(1, trades.size());
        assertEquals(100, trades.get(0).getPrice());
    }

    @Test
    public void testSweepAcrossMultiplePriceLevels() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new LimitOrder('S', 1, 100, 20));
        orderBook.addOrder(new LimitOrder('S', 2, 101, 30));
        orderBook.addOrder(new LimitOrder('S', 3, 102, 50));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 4, 102, 100));
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
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 25, 10));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 2, 100, 50));
        assertEquals(1, trades.size());
        assertEquals(25, trades.get(0).getQuantity());
        assertEquals(0, orderBook.getSellOrderCount(100));
        assertEquals(1, orderBook.getBuyOrderCount(100));
    }

    @Test
    public void testMultipleIcebergsAtSamePrice() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 25, 10));
        orderBook.addOrder(new IcebergOrder('S', 2, 100, 25, 10));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 3, 100, 15));
        assertEquals(2, trades.size());
        assertEquals(1, trades.get(0).getSellOrderId());
        assertEquals(2, trades.get(1).getSellOrderId());
    }


    @Test
    public void testIcebergMovesToBackOfQueue() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 30, 10));
        orderBook.addOrder(new LimitOrder('S', 2, 100, 15));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 3, 100, 10));
        assertEquals(1, trades.size());
        assertEquals(2, orderBook.getSellRows().get(0).getId());
        assertEquals(1, orderBook.getSellRows().get(1).getId());
    }

    @Test
    public void testIcebergWithPeakSizeEqualToTotalQuantity() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 30, 30));
        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 2, 100, 30));
        assertEquals(1, trades.size());
        assertEquals(0, orderBook.getSellRows().size());
    }

    @Test
    public void testBothIcebergsReplenishAndContinueMatching() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 20, 10));
        List<Trade> trades = orderBook.addOrder(new IcebergOrder('B', 2, 100, 30, 10));
        
        assertEquals(1, trades.size());
        assertEquals(20, trades.get(0).getQuantity());
        assertEquals(0, orderBook.getSellRows().size());
        assertEquals(1, orderBook.getBuyRows().size());
        assertEquals(10, orderBook.getBuyRows().get(0).getVolume());
    }

    @Test
    public void testMultipleIcebergsInQueueReplenishment() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 10, 5));
        orderBook.addOrder(new IcebergOrder('S', 2, 100, 15, 5));
        List<Trade> trades = orderBook.addOrder(new IcebergOrder('B', 3, 100, 25, 5));
        
        int totalQuantity = trades.stream().mapToInt(Trade::getQuantity).sum();
        assertEquals(25, totalQuantity);
        assertEquals(0, orderBook.getSellRows().size());
        assertEquals(0, orderBook.getBuyRows().size());
    }


    @Test
    public void testIcebergMatchesMultipleSellOrders() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new LimitOrder('S', 1, 10, 10));
        orderBook.addOrder(new LimitOrder('S', 2, 10, 11));
        List<Trade> trades = orderBook.addOrder(new IcebergOrder('B', 3, 10, 100, 10));
        
        int totalMatched = trades.stream().mapToInt(Trade::getQuantity).sum();
        assertEquals(21, totalMatched);
        assertEquals(0, orderBook.getSellRows().size());
        assertEquals(1, orderBook.getBuyRows().size());
        assertEquals(10, orderBook.getBuyRows().get(0).getVolume());
        assertEquals(2, trades.size());
    }

    @Test
    public void testIncomingMatchesIcebergThenNextOrder() {
    OrderBook orderBook = new OrderBook();
    orderBook.addOrder(new IcebergOrder('S', 1, 100, 30, 10));
    orderBook.addOrder(new LimitOrder('S', 2, 100, 15));
    List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 3, 100, 25));
    
    assertEquals(2, trades.size());
    assertEquals(10, trades.get(0).getQuantity()); 
    assertEquals(15, trades.get(1).getQuantity()); 
    assertEquals(1, orderBook.getSellOrderCount(100)); 
    assertEquals(10, orderBook.getSellRows().get(0).getVolume()); 
}

    @Test
    public void testIcebergRestingVisibleQuantityWhenNoMatch() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('B', 10, 200, 100, 15));
        assertEquals(1, orderBook.getBuyOrderCount(200));
        assertEquals(15, orderBook.getBuyRows().get(0).getVolume());
        assertEquals(10, orderBook.getBuyRows().get(0).getId());
    }

    @Test
    public void testIcebergRequeueBehindAllSamePriceOrders() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new IcebergOrder('S', 1, 100, 10, 5));
        orderBook.addOrder(new LimitOrder('S', 2, 100, 5));
        orderBook.addOrder(new LimitOrder('S', 3, 100, 5));
        orderBook.addOrder(new LimitOrder('B', 4, 100, 5));
        List<BookRow> sells = orderBook.getSellRows();
        assertEquals(3, sells.size());
        assertEquals(2, sells.get(0).getId());
        assertEquals(3, sells.get(1).getId());
        assertEquals(1, sells.get(2).getId());
    }

    @Test
    public void testFIFOWithinSamePriceLevel() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new LimitOrder('S', 1, 100, 5));
        orderBook.addOrder(new LimitOrder('S', 2, 100, 7));

        List<Trade> trades = orderBook.addOrder(new LimitOrder('B', 3, 100, 10));
        assertEquals(2, trades.size());
        assertEquals(1, trades.get(0).getSellOrderId());
        assertEquals(2, trades.get(1).getSellOrderId());
        assertEquals(5, trades.get(0).getQuantity());
        assertEquals(5, trades.get(1).getQuantity());
        assertEquals(1, orderBook.getSellOrderCount(100));
        assertEquals(2, orderBook.getSellRows().get(0).getId());
        assertEquals(2, orderBook.getSellRows().get(0).getVolume());
    }

    @Test
    public void testAggressiveIcebergOrderEntry() {
        OrderBook orderBook = new OrderBook();
        orderBook.addOrder(new LimitOrder('B', 1, 99, 50000));
        orderBook.addOrder(new LimitOrder('B', 2, 98, 25500));
        orderBook.addOrder(new LimitOrder('S', 3, 100, 10000));
        orderBook.addOrder(new LimitOrder('S', 4, 100, 7500));
        orderBook.addOrder(new LimitOrder('S', 5, 101, 20000));
        List<Trade> trades = orderBook.addOrder(new IcebergOrder('B', 6, 100, 100000, 10000));
        
        // Assert trades
        assertEquals(2, trades.size());
        
        // Match against Sell Order 3 (10,000 @ 100)
        assertEquals(6, trades.get(0).getBuyOrderId());
        assertEquals(3, trades.get(0).getSellOrderId());
        assertEquals(100, trades.get(0).getPrice());
        assertEquals(10000, trades.get(0).getQuantity());

        // Match against Sell Order 4 (7,500 @ 100)
        assertEquals(6, trades.get(1).getBuyOrderId());
        assertEquals(4, trades.get(1).getSellOrderId());
        assertEquals(100, trades.get(1).getPrice());
        assertEquals(7500, trades.get(1).getQuantity());

        // Assert Order Book State
        List<BookRow> buyRows = orderBook.getBuyRows();
        List<BookRow> sellRows = orderBook.getSellRows();

        // Verify Buy Side
        // Expecting:
        // 1. Iceberg Order 6 (Peak 10,000 @ 100)
        // 2. Limit Order 1 (50,000 @ 99)
        // 3. Limit Order 2 (25,500 @ 98)
        assertEquals(3, buyRows.size());

        // Row 1: The Iceberg Order (Resting at 100p with peak 10,000)
        assertEquals(6, buyRows.get(0).getId());
        assertEquals(10000, buyRows.get(0).getVolume());
        assertEquals(100, buyRows.get(0).getPrice());

        // Row 2: Existing Buy Order
        assertEquals(1, buyRows.get(1).getId());
        assertEquals(50000, buyRows.get(1).getVolume());
        assertEquals(99, buyRows.get(1).getPrice());

        // Row 3: Existing Buy Order
        assertEquals(2, buyRows.get(2).getId());
        assertEquals(25500, buyRows.get(2).getVolume());
        assertEquals(98, buyRows.get(2).getPrice());

        // Verify Sell Side
        // Expecting:
        // 1. Limit Order 5 (20,000 @ 101) - Orders at 100 were consumed
        assertEquals(1, sellRows.size());
        assertEquals(5, sellRows.get(0).getId());
        assertEquals(20000, sellRows.get(0).getVolume());
        assertEquals(101, sellRows.get(0).getPrice());
    }

    @Test
    public void testPassiveIcebergOrderExecution() {
        OrderBook orderBook = new OrderBook();
        // Setup initial state from previous test
        orderBook.addOrder(new LimitOrder('B', 1, 99, 50000));
        orderBook.addOrder(new LimitOrder('B', 2, 98, 25500));
        orderBook.addOrder(new LimitOrder('S', 5, 101, 20000));
        // Add Iceberg: 100,000 total, 17,500 matched already in aggressive phase, so 82,500 remaining.
        // Peak 10,000.
        // But to simulate exactly "Passive execution", we just add an iceberg sitting there.
        // The scenario says "remaining iceberg size is 82,500 shares. The first peak of 10,000 shares is then entered".
        orderBook.addOrder(new IcebergOrder('B', 6, 100, 82500, 10000));

        // Action: Sell 10,000 @ 100 (At Best)
        List<Trade> trades1 = orderBook.addOrder(new LimitOrder('S', 7, 100, 10000));
        
        // Result: Full visible peak executed.
        assertEquals(1, trades1.size());
        assertEquals(10000, trades1.get(0).getQuantity());
        assertEquals(6, trades1.get(0).getBuyOrderId());

        // Check state: Total remaining 72,500. Visible 10,000.
        assertEquals(10000, orderBook.getBuyRows().get(0).getVolume());
        assertEquals(6, orderBook.getBuyRows().get(0).getId());

        // Action: Sell 11,000 @ 100 (At Best)
        List<Trade> trades2 = orderBook.addOrder(new LimitOrder('S', 8, 100, 11000));

        // Result: Single trade message for 11,000 (10,000 visible + 1,000 hidden)
        assertEquals(1, trades2.size());
        assertEquals(11000, trades2.get(0).getQuantity());
        assertEquals(6, trades2.get(0).getBuyOrderId());

        // Check state: Total remaining 61,500. Visible 9,000.
        assertEquals(9000, orderBook.getBuyRows().get(0).getVolume());
        assertEquals(6, orderBook.getBuyRows().get(0).getId());
    }

    @Test
    public void testMultipleIcebergExecution() {
        OrderBook orderBook = new OrderBook();
        
        // Setup state to match previous tests exactly
        // 1. Initial Sells for Aggressive match
        orderBook.addOrder(new LimitOrder('S', 3, 100, 10000));
        orderBook.addOrder(new LimitOrder('S', 4, 100, 7500));
        // Other context orders
        orderBook.addOrder(new LimitOrder('S', 5, 101, 20000)); 
        orderBook.addOrder(new LimitOrder('B', 1, 99, 50000)); 
        
        // 2. Add Iceberg A - Aggressive entry matches 17,500
        orderBook.addOrder(new IcebergOrder('B', 6, 100, 100000, 10000)); 
        // State: 82,500 rem, 10,000 visible.

        // 3. Passive executions
        orderBook.addOrder(new LimitOrder('S', 102, 100, 10000)); 
        // State: 72,500 rem, 10,000 visible.
        
        orderBook.addOrder(new LimitOrder('S', 103, 100, 11000)); 
        // State: 61,500 rem, 9,000 visible.

        // Verify start state for this test
        assertEquals(9000, orderBook.getBuyRows().get(0).getVolume());
        assertEquals(6, orderBook.getBuyRows().get(0).getId()); // Iceberg A

        // Enter Iceberg B: Buy 50,000 @ 100, Peak 20,000
        orderBook.addOrder(new IcebergOrder('B', 7, 100, 50000, 20000)); // Iceberg B

        // Verify Book: A (9,000), B (20,000)
        List<BookRow> rows = orderBook.getBuyRows();
        assertEquals(3, rows.size());
        assertEquals(6, rows.get(0).getId());
        assertEquals(9000, rows.get(0).getVolume());
        assertEquals(7, rows.get(1).getId());
        assertEquals(20000, rows.get(1).getVolume());
        assertEquals(50000, rows.get(2).getVolume());

        // Action: Sell 35,000 @ 100
        List<Trade> trades = orderBook.addOrder(new LimitOrder('S', 8, 100, 35000));

        // Result:
        // 1. Match A (visible 9,000). A replenishes 10,000. Moves to back. Order: B, A.
        // 2. Match B (visible 20,000). B replenishes 20,000. Moves to back. Order: A, B.
        // 3. Match A (remaining incoming 6,000). A visible becomes 4,000.
        
        // Trades generated:
        // Trade 1: Sell 8 vs Buy 6 (A). Qty: 9,000 + 6,000 = 15,000.
        // Trade 2: Sell 8 vs Buy 7 (B). Qty: 20,000.
        
        assertEquals(2, trades.size());
        
        // Trade for A (6)
        Trade tradeA = trades.stream().filter(t -> t.getBuyOrderId() == 6).findFirst().orElseThrow();
        assertEquals(15000, tradeA.getQuantity());
        
        // Trade for B (7)
        Trade tradeB = trades.stream().filter(t -> t.getBuyOrderId() == 7).findFirst().orElseThrow();
        assertEquals(20000, tradeB.getQuantity());

        // Verify Final Book State
        // Expected: A (4,000), B (20,000)
        rows = orderBook.getBuyRows();
        assertEquals(3, rows.size());
        assertEquals(6, rows.get(0).getId());
        assertEquals(4000, rows.get(0).getVolume());
        
        assertEquals(7, rows.get(1).getId());
        assertEquals(20000, rows.get(1).getVolume());

    }
}
