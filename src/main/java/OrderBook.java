import java.util.*;

public class OrderBook {
    // Buy side (Price DESC)
    private TreeMap<Integer, ArrayDeque<Order>> buySide = new TreeMap<>(Collections.reverseOrder());
    
    // Sell side (Price ASC)
    private TreeMap<Integer, ArrayDeque<Order>> sellSide = new TreeMap<>();
    
    // Main public method for adding orders
    public List<Trade> addOrder(Order incoming) {
        // 1. Process matches against the opposite side
        List<Trade> trades = processMatches(incoming);

        // 2. Add remaining quantity to the book
        if (incoming.getTotalQuantity() > 0) {
            if (incoming.getSide() == 'B') {
                buySide.computeIfAbsent(incoming.getPrice(), k -> new ArrayDeque<>()).addLast(incoming);
            } else {
                sellSide.computeIfAbsent(incoming.getPrice(), k -> new ArrayDeque<>()).addLast(incoming);
            }
        }
        
        return trades;
    }

    // Private helper method for addOrder
    private List<Trade> processMatches(Order incomingOrder) {
        char side = incomingOrder.getSide();
        TreeMap<Integer, ArrayDeque<Order>> oppositeSide = (side == 'B') ? sellSide : buySide;

        List<Trade> trades = new ArrayList<>();
        // Map to track total quantity matched against each existing order ID during this incomingOrder's match cycle.
        // Key: Order ID, Value: QuantityMatched
        Map<Integer, Integer> matchQuantities = new LinkedHashMap<>();
        Map<Integer, Integer> matchPrices = new HashMap<>();

        while (incomingOrder.getTotalQuantity() > 0 && !oppositeSide.isEmpty()) {
            Map.Entry<Integer, ArrayDeque<Order>> bestEntry = oppositeSide.firstEntry();
            int bestPrice = bestEntry.getKey();

            // Check if price matches
            if (side == 'B' && incomingOrder.getPrice() < bestPrice) break;
            if (side == 'S' && incomingOrder.getPrice() > bestPrice) break;

            ArrayDeque<Order> queue = bestEntry.getValue();
            Order existingOrder = queue.peekFirst();
            
            if (existingOrder == null) {
                oppositeSide.remove(bestPrice);
                continue;
            }

            int matchQty = Math.min(incomingOrder.getTotalQuantity(), existingOrder.getVisibleQuantity());
            if (matchQty > 0) {
                incomingOrder.reduceQuantity(matchQty);
                existingOrder.reduceQuantity(matchQty);
                
                matchQuantities.put(existingOrder.getId(), matchQuantities.getOrDefault(existingOrder.getId(), 0) + matchQty);
                matchPrices.put(existingOrder.getId(), bestPrice);
            }

            if (existingOrder.getVisibleQuantity() == 0) {
                queue.removeFirst();
                if (existingOrder.getTotalQuantity() > 0) {
                    existingOrder.replenish();
                    queue.addLast(existingOrder);
                }
            }

            if (queue.isEmpty()) {
                oppositeSide.remove(bestPrice);
            }
        }

        // Convert the match records into Trade objects
        for (Integer existingOrderId : matchQuantities.keySet()) {
            int qty = matchQuantities.get(existingOrderId);
            int price = matchPrices.get(existingOrderId);
            
            if (side == 'B') {
                trades.add(new Trade(incomingOrder.getId(), existingOrderId, price, qty));
            } else {
                trades.add(new Trade(existingOrderId, incomingOrder.getId(), price, qty));
            }
        }
        
        return trades;
    }

    // Public methods for retrieving book rows
    public List<BookRow> getBuyRows() {
        return createBookRows(buySide);
    }

    public List<BookRow> getSellRows() {
        return createBookRows(sellSide);
    }

    // Private helper method for getBuyRows and getSellRows
    private List<BookRow> createBookRows(TreeMap<Integer, ArrayDeque<Order>> orderSide) {
        List<BookRow> rows = new ArrayList<>();
        for (Map.Entry<Integer, ArrayDeque<Order>> entry : orderSide.entrySet()) {
            for (Order o : entry.getValue()) {
                rows.add(new BookRow(o.getId(), o.getVisibleQuantity(), entry.getKey()));
            }
        }
        return rows;
    }

    // Helper methods for testing
    public int getBuyOrderCount(int price) {
        return buySide.containsKey(price) ? buySide.get(price).size() : 0;
    }

    public int getSellOrderCount(int price) {
        return sellSide.containsKey(price) ? sellSide.get(price).size() : 0;
    }
}