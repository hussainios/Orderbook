public abstract class Order {
    
    protected char side; // 'B' for buy, 'S' for sell
    protected int id;
    protected int price;
    protected int totalQuantity;

    public Order(char side, int id, int price, int totalQuantity) {
        this.side = side;
        this.id = id;
        this.price = price;
        this.totalQuantity = totalQuantity;
    }

    public char getSide() { return side; }
    public int getId() { return id; }
    public int getPrice() { return price; }
    public int getTotalQuantity() { return totalQuantity; }

    /**
     * Returns the quantity currently visible in the order book.
     */
    public abstract int getVisibleQuantity();

    /**
     * Reduces the total quantity of the order.
     * Subclasses must handle how this affects their visible quantity.
     *
     * @param amount the quantity to reduce by
     */
    public abstract void reduceQuantity(int amount);

    /**
     * Replenishes the visible quantity.
     * For Limit orders, this does nothing.
     * For Iceberg orders, this resets visible quantity based on peak size and remaining total.
     */
    public abstract void replenish();
}
