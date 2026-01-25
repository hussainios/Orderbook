import java.util.*;

public class OrderBook {
    // Buy side (Price DESC)
    private TreeMap<Integer, ArrayDeque<Order>> buySide = new TreeMap<>(Collections.reverseOrder());
    
    // Sell side (Price ASC)
    private TreeMap<Integer, ArrayDeque<Order>> sellSide = new TreeMap<>();
    
    // Outputter for trades and book display
    private OrderOutputter outputter = new OrderOutputter();

    public void addOrder(Order incoming) {
        // 1. Process matches against the opposite side
        processMatches(incoming);

        // 2. Add remaining quantity to the book
        if (incoming.getTotalQuantity() > 0) {
            if (incoming.getSide() == 'B') {
                buySide.computeIfAbsent(incoming.getPrice(), k -> new ArrayDeque<>()).addLast(incoming);
            } else {
                sellSide.computeIfAbsent(incoming.getPrice(), k -> new ArrayDeque<>()).addLast(incoming);
            }
        }

        // 3. Print the book after every order insertion
        outputter.printBook(buySide, sellSide);
    }

    private void processMatches(Order aggressor) {
        char side = aggressor.getSide();
        TreeMap<Integer, ArrayDeque<Order>> oppositeSide = (side == 'B') ? sellSide : buySide;

        // Map to track total quantity matched against each passive order ID during this aggressor's match cycle.
        // We also need to remember the price for each trade.
        // Key: Order ID, Value: TradeRecord(price, totalQuantity)
        Map<Integer, TradeRecord> trades = new LinkedHashMap<>();

        while (aggressor.getTotalQuantity() > 0 && !oppositeSide.isEmpty()) {
            Map.Entry<Integer, ArrayDeque<Order>> bestEntry = oppositeSide.firstEntry();
            int bestPrice = bestEntry.getKey();

            // Check if price matches
            if (side == 'B' && aggressor.getPrice() < bestPrice) break;
            if (side == 'S' && aggressor.getPrice() > bestPrice) break;

            ArrayDeque<Order> queue = bestEntry.getValue();
            Order passive = queue.peekFirst();
            
            if (passive == null) {
                oppositeSide.remove(bestPrice);
                continue;
            }

            int matchQty = Math.min(aggressor.getTotalQuantity(), passive.getVisibleQuantity());
            if (matchQty > 0) {
                aggressor.reduceQuantity(matchQty);
                passive.reduceQuantity(matchQty);
                
                TradeRecord record = trades.get(passive.getId());
                if (record == null) {
                    trades.put(passive.getId(), new TradeRecord(bestPrice, matchQty));
                } else {
                    record.quantity += matchQty;
                }
            }

            if (passive.getVisibleQuantity() == 0) {
                queue.removeFirst();
                if (passive.getTotalQuantity() > 0) {
                    passive.replenish();
                    queue.addLast(passive);
                }
            }

            if (queue.isEmpty()) {
                oppositeSide.remove(bestPrice);
            }
        }

        // After all matching for this aggressor is done, emit the trades
        for (Map.Entry<Integer, TradeRecord> entry : trades.entrySet()) {
            outputter.printTrade(aggressor, entry.getKey(), entry.getValue().price, entry.getValue().quantity);
        }
    }

    // Helper methods for testing
    public int getBuyOrderCount(int price) {
        return buySide.containsKey(price) ? buySide.get(price).size() : 0;
    }

    public int getSellOrderCount(int price) {
        return sellSide.containsKey(price) ? sellSide.get(price).size() : 0;
    }
}

class TradeRecord {
    int price;
    int quantity;
    TradeRecord(int price, int quantity) {
        this.price = price;
        this.quantity = quantity;
    }
}
