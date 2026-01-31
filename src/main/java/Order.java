public class Order {
    
    private char side; // 'B' for buy, 'S' for sell
    private int id;
    private int price;
    private int totalQuantity;
    private int peakSize; // 0 for limit orders, > 0 for iceberg orders
    private int visibleQuantity;

    public char getSide() { return side; }
    public int getId() { return id; }
    public int getPrice() { return price; }
    public int getTotalQuantity() { return totalQuantity; }
    public int getPeakSize() { return peakSize; }
    public int getVisibleQuantity() { return visibleQuantity; }

    Order(char side, int id, int price, int totalQuantity, int peakSize, int visibleQuantity) {
        this.side = side;
        this.id = id;
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.peakSize = peakSize;
        this.visibleQuantity = visibleQuantity;
    }
    
    /**
     * Reduces the total and visible quantity of the order by the specified amount.
     *
     * @param amount the quantity to reduce by
     */
    public void reduceQuantity(int amount) {
        this.totalQuantity -= amount;
        this.visibleQuantity -= amount;
    }

    /**
     * Reduces the total quantity of the incoming order by the specified amount.
     * Ensures visible quantity does not exceed total quantity.
     *
     * @param amount the quantity to reduce by
     */
    public void reduceIncomingQuantity(int amount) {
        this.totalQuantity -= amount;
        if (this.visibleQuantity > this.totalQuantity) {
            this.visibleQuantity = this.totalQuantity;
        }
    }

    /**
     * Replenishes the visible quantity for iceberg orders from the remaining total quantity.
     */
    public void replenish() {
        if (peakSize > 0 && totalQuantity > 0) {
            this.visibleQuantity = Math.min(totalQuantity, peakSize);
        }
    }

    public static Order limit(char side, int id, int price, int quantity) {
        return new Order(side, id, price, quantity, 0, quantity);
    }
    
    public static Order iceberg(char side, int id, int price, int totalQuantity, int peakSize) {
        int visibleQuantity = Math.min(totalQuantity, peakSize);
        return new Order(side, id, price, totalQuantity, peakSize, visibleQuantity); 
    }

}
