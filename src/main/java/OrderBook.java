import java.util.*;

public class OrderBook {
    private static class MatchInfo {
        private int quantity;
        private int price;

        private MatchInfo(int quantity, int price) {
            this.quantity = quantity;
            this.price = price;
        }

        private void addQuantity(int matchQuantity) {
            this.quantity += matchQuantity;
        }
    }
    // price : Queue[OrderID]  Buy side (Price DESC)
    private TreeMap<Integer, ArrayDeque<Order>> buySide = new TreeMap<>(Collections.reverseOrder());

    // price : Queue[OrderID]  Sell side (Price ASC)
    private TreeMap<Integer, ArrayDeque<Order>> sellSide = new TreeMap<>();

    /**
     * Adds an order, performs matching, and returns any resulting trades.
     */
    public List<Trade> addOrder(Order incomingOrder) {

        List<Trade> trades = processMatches(incomingOrder);

        // Add remaining quantity to the book if it has remaining quantity after matching
        if (incomingOrder.getTotalQuantity() > 0) {
            incomingOrder.replenish(); // Reset visible quantity for icebergs
            if (incomingOrder.getSide() == 'B') {
                buySide.computeIfAbsent(incomingOrder.getPrice(), k -> new ArrayDeque<>()).addLast(incomingOrder);
            } else {
                sellSide.computeIfAbsent(incomingOrder.getPrice(), k -> new ArrayDeque<>()).addLast(incomingOrder);
            }
        }

        return trades;
    }

    /**
     * Processes matches for the incoming order, keep matching until there are no more matches available.
     *
     * @param incomingOrder the order to match
     * @return a list of trades resulting from the matches
     */
    private List<Trade> processMatches(Order incomingOrder) {
        char side = incomingOrder.getSide();
        // Sorted hashmap price : Queue[OrderID]
        TreeMap<Integer, ArrayDeque<Order>> oppositeSide = determineOppositeSide(side);
        // Ordered hashmap orderID : (quantity, price)
        Map<Integer, MatchInfo> matchInfoByOrderId = new LinkedHashMap<>();

        while (incomingOrder.getTotalQuantity() > 0 && !oppositeSide.isEmpty()) {

            // Get the best price and check if it overlaps
            Map.Entry<Integer, ArrayDeque<Order>> bestEntry = oppositeSide.firstEntry();
            int bestPrice = bestEntry.getKey();
            if (!checkPriceOverlap(side, incomingOrder.getPrice(), bestPrice)) break;

            ArrayDeque<Order> queue = bestEntry.getValue();
            Order existingOrder = queue.peekFirst();

            // Match against the existing order's visible quantity
            // Polymorphism handles whether this is a Limit or Iceberg order
            int existingVisible = existingOrder.getVisibleQuantity();
            int incomingTotal = incomingOrder.getTotalQuantity();

            // Calculate the amount traded in this match
            int matchQuantity = Math.min(incomingTotal, existingVisible);
            
            if (matchQuantity == 0) {
                 // Should not happen if logic is correct, but safe to remove if empty
                if (existingVisible == 0) {
                     queue.removeFirst();
                     if (queue.isEmpty()) {
                        oppositeSide.remove(bestPrice);
                     }
                     continue;
                }
            }

            executeMatch(incomingOrder, existingOrder, matchQuantity, bestPrice, matchInfoByOrderId);

            // Handle replenishment or removal of the existing order
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

        return createTradesFromMatches(side, incomingOrder, matchInfoByOrderId);
    }

    public List<BookRow> getBuyRows() {
        return createBookRows(buySide);
    }

    public List<BookRow> getSellRows() {
        return createBookRows(sellSide);
    }

    /**
     * Creates book rows from the given side of the order book.
     *
     * @param orderSide A sorted Hashmap with the Prices : Queue[OrderID] representing one side of the book
     * @return a list of {@link BookRow} objects
     */
    private List<BookRow> createBookRows(TreeMap<Integer, ArrayDeque<Order>> orderSide) {
        List<BookRow> rows = new ArrayList<>();
        for (Map.Entry<Integer, ArrayDeque<Order>> entry : orderSide.entrySet()) {
            for (Order o : entry.getValue()) {
                // Polymorphic call to getVisibleQuantity()
                rows.add(new BookRow(o.getId(), o.getVisibleQuantity(), entry.getKey()));
            }
        }
        return rows;
    }

    // Private helpers for testing
    public int getBuyOrderCount(int price) {
        return buySide.containsKey(price) ? buySide.get(price).size() : 0;
    }

    public int getSellOrderCount(int price) {
        return sellSide.containsKey(price) ? sellSide.get(price).size() : 0;
    }

    /**
     * Returns the opposite side map for the given side.
     */
    private TreeMap<Integer, ArrayDeque<Order>> determineOppositeSide(char side) {
        return (side == 'B') ? sellSide : buySide;
    }

    /**
     * Applies a match and records trade info.
     */
    private void executeMatch(Order incomingOrder, Order existingOrder, int matchQuantity, int price, Map<Integer, MatchInfo> matchInfoByOrderId) {
        incomingOrder.reduceQuantity(matchQuantity);
        existingOrder.reduceQuantity(matchQuantity);

        MatchInfo matchInfo = matchInfoByOrderId.get(existingOrder.getId());
        if (matchInfo == null) {
            matchInfo = new MatchInfo(0, price);
            matchInfoByOrderId.put(existingOrder.getId(), matchInfo);
        }
        matchInfo.addQuantity(matchQuantity);
        matchInfo.price = price;
    }

    /**
     * Checks if the incoming order's price matches the best price on the opposite side.
     */
    private boolean checkPriceOverlap(char side, int incomingOrderPrice, int bestPrice) {
        if (side == 'B' && incomingOrderPrice < bestPrice) return false;
        if (side == 'S' && incomingOrderPrice > bestPrice) return false;
        return true;
    }

    /**
     * Creates Trade objects from the accumulated match information.
     */
    private List<Trade> createTradesFromMatches(char side, Order incomingOrder, Map<Integer, MatchInfo> matchInfoByOrderId) {
        List<Trade> trades = new ArrayList<>();
        for (Map.Entry<Integer, MatchInfo> entry : matchInfoByOrderId.entrySet()) {
            Integer existingOrderId = entry.getKey();
            MatchInfo matchInfo = entry.getValue();
            int qty = matchInfo.quantity;
            int price = matchInfo.price;

            if (side == 'B') {
                trades.add(new Trade(incomingOrder.getId(), existingOrderId, price, qty));
            } else {
                trades.add(new Trade(existingOrderId, incomingOrder.getId(), price, qty));
            }
        }
        return trades;
    }
}
