public class IcebergOrder extends Order {
    
    private final int peakSize;
    private int currentVisibleQuantity;

    public IcebergOrder(char side, int id, int price, int totalQuantity, int peakSize) {
        super(side, id, price, totalQuantity);
        this.peakSize = peakSize;
        this.currentVisibleQuantity = Math.min(totalQuantity, peakSize);
    }

    public int getPeakSize() {
        return peakSize;
    }

    @Override
    public int getVisibleQuantity() {
        return currentVisibleQuantity;
    }

    @Override
    public void reduceQuantity(int amount) {
        this.totalQuantity -= amount;
        this.currentVisibleQuantity -= amount;
        if (this.currentVisibleQuantity < 0) {
            this.currentVisibleQuantity = 0;
        }
    }

    /**
     * Replenishes the visible quantity from the remaining total quantity.
     * This is called when the visible quantity is exhausted and the order is in the book.
     */
    public void replenish() {
        if (totalQuantity > 0) {
            this.currentVisibleQuantity = Math.min(totalQuantity, peakSize);
        } else {
            this.currentVisibleQuantity = 0;
        }
    }
}
