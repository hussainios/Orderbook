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
    
    public void reduceQuantity(int amount) {
        this.totalQuantity -= amount;
        this.visibleQuantity -= amount;
    }

    public void replenish() {
        if (peakSize > 0 && totalQuantity > 0) {
            this.visibleQuantity = Math.min(totalQuantity, peakSize);
        }
    }

    public static Order limit(char side, int id, int price, int quantity) {
        return new Order(side, id, price, quantity, 0, quantity);
    }
    
    public static Order iceberg(char side, int id, int price, int totalQuantity, int peakSize) {
        return new Order(side, id, price, totalQuantity, peakSize, Math.min(totalQuantity, peakSize)); // Second PeakSize is the visible quantity
    }

}
