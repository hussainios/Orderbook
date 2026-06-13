import java.util.*;

public class OrderBook {
    private static class OrderNode {
        private final Order order;
        private OrderNode prev;
        private OrderNode next;

        private OrderNode(Order order) {
            this.order = order;
        }
    }

    private static class PriceLevel implements Iterable<Order> {
        private OrderNode head;
        private OrderNode tail;
        private int size;

        private OrderNode addLast(Order order) {
            OrderNode node = new OrderNode(order);
            if (tail == null) {
                head = node;
                tail = node;
            } else {
                tail.next = node;
                node.prev = tail;
                tail = node;
            }
            size++;
            return node;
        }

        private Order peekFirst() {
            return head == null ? null : head.order;
        }

        private void remove(OrderNode node) {
            if (node.prev == null) {
                head = node.next;
            } else {
                node.prev.next = node.next;
            }

            if (node.next == null) {
                tail = node.prev;
            } else {
                node.next.prev = node.prev;
            }

            node.prev = null;
            node.next = null;
            size--;
        }

        private Order removeFirst() {
            OrderNode node = head;
            if (node == null) {
                return null;
            }
            remove(node);
            return node.order;
        }

        private boolean isEmpty() {
            return size == 0;
        }

        private int size() {
            return size;
        }

        @Override
        public Iterator<Order> iterator() {
            return new Iterator<Order>() {
                private OrderNode current = head;

                @Override
                public boolean hasNext() {
                    return current != null;
                }

                @Override
                public Order next() {
                    if (current == null) {
                        throw new NoSuchElementException();
                    }
                    Order order = current.order;
                    current = current.next;
                    return order;
                }
            };
        }
    }

    /**
     * Metadata for a resting live order so cancel can jump directly to the
     * correct side, price level, and queue node.
     */
    private static class LiveOrderRef {
        private final char side;
        private final int price;
        private final OrderNode node;

        private LiveOrderRef(char side, int price, OrderNode node) {
            this.side = side;
            this.price = price;
            this.node = node;
        }
    }

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
    private TreeMap<Integer, PriceLevel> buySide = new TreeMap<>(Collections.reverseOrder());

    // price : Queue[OrderID]  Sell side (Price ASC)
    private TreeMap<Integer, PriceLevel> sellSide = new TreeMap<>();

    // orderId : resting live order metadata
    private Map<Integer, LiveOrderRef> liveOrders = new HashMap<>();

    /**
     * Adds an order, performs matching, and returns any resulting trades.
     */
    public List<Trade> addOrder(Order incomingOrder) {
        if (hasLiveOrder(incomingOrder.getId())) {
            throw new IllegalArgumentException("Order id is already live: " + incomingOrder.getId());
        }

        List<Trade> trades = processMatches(incomingOrder);

        // Add remaining quantity to the book if it has remaining quantity after matching
        if (incomingOrder.getTotalQuantity() > 0) {
            incomingOrder.replenish(); 

            OrderNode node;
            if (incomingOrder.getSide() == 'B') {
                node = buySide.computeIfAbsent(incomingOrder.getPrice(), k -> new PriceLevel()).addLast(incomingOrder);
            } else {
                node = sellSide.computeIfAbsent(incomingOrder.getPrice(), k -> new PriceLevel()).addLast(incomingOrder);
            }
            liveOrders.put(incomingOrder.getId(), new LiveOrderRef(incomingOrder.getSide(), incomingOrder.getPrice(), node));
        }

        return trades;
    }

    public boolean cancelOrder(int orderId) {
        LiveOrderRef liveOrderRef = liveOrders.get(orderId);
        if (liveOrderRef == null) {
            return false;
        }

        TreeMap<Integer, PriceLevel> sideMap = getSideMap(liveOrderRef.side);
        PriceLevel priceLevelQueue = sideMap.get(liveOrderRef.price);
        if (priceLevelQueue == null) {
            return false;
        }

        priceLevelQueue.remove(liveOrderRef.node);
        liveOrders.remove(orderId);
        if (priceLevelQueue.isEmpty()) {
            sideMap.remove(liveOrderRef.price);
        }
        return true;
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
        TreeMap<Integer, PriceLevel> oppositeSide = determineOppositeSide(side);
        // Ordered hashmap orderID : (quantity, price)
        Map<Integer, MatchInfo> matchInfoByOrderId = new LinkedHashMap<>();

        while (incomingOrder.getTotalQuantity() > 0 && !oppositeSide.isEmpty()) {

            // Get the best price and check if it overlaps
            Map.Entry<Integer, PriceLevel> bestEntry = oppositeSide.firstEntry();
            int bestPrice = bestEntry.getKey();
            if (!checkPriceOverlap(side, incomingOrder.getPrice(), bestPrice)) break;

            PriceLevel queue = bestEntry.getValue();
            Order existingOrder = queue.peekFirst();

            // Match the incoming order's total quantity against the existing order's visible quantity
            int existingVisible = existingOrder.getVisibleQuantity();
            int incomingTotal = incomingOrder.getTotalQuantity();

            int matchQuantity = Math.min(incomingTotal, existingVisible);
            executeMatch(incomingOrder, existingOrder, matchQuantity, bestPrice, matchInfoByOrderId);

            // Handle replenishment or removal of the existing order
            if (existingOrder.getVisibleQuantity() == 0) {
                queue.removeFirst();
                if (existingOrder.getTotalQuantity() > 0) {
                    existingOrder.replenish();
                    OrderNode replenishedNode = queue.addLast(existingOrder);
                    liveOrders.put(existingOrder.getId(), new LiveOrderRef(existingOrder.getSide(), bestPrice, replenishedNode));
                } else {
                    liveOrders.remove(existingOrder.getId());
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
    private List<BookRow> createBookRows(TreeMap<Integer, PriceLevel> orderSide) {
        List<BookRow> rows = new ArrayList<>();
        for (Map.Entry<Integer, PriceLevel> entry : orderSide.entrySet()) {
            for (Order o : entry.getValue()) {
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

    public boolean hasLiveOrder(int orderId) {
        return liveOrders.containsKey(orderId);
    }

    /**
     * Returns the opposite side map for the given side.
     */
    private TreeMap<Integer, PriceLevel> determineOppositeSide(char side) {
        return (side == 'B') ? sellSide : buySide;
    }

    /**
     * Returns the book side for the given side.
     */
    private TreeMap<Integer, PriceLevel> getSideMap(char side) {
        return (side == 'B') ? buySide : sellSide;
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
