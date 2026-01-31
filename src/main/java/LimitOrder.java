public class LimitOrder extends Order {

    public LimitOrder(char side, int id, int price, int totalQuantity) {
        super(side, id, price, totalQuantity);
    }

    @Override
    public int getVisibleQuantity() {
        return totalQuantity;
    }

    @Override
    public void reduceQuantity(int amount) {
        this.totalQuantity -= amount;
    }

    @Override
    public void replenish() {
        // No replenishment needed for limit orders
    }
}
