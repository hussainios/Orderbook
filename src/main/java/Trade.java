public class Trade {
    private final int buyOrderId;
    private final int sellOrderId;
    private final int price;
    private final int quantity;

    /**
     * Trade record for output.
     */
    public Trade(int buyOrderId, int sellOrderId, int price, int quantity) {
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.quantity = quantity;
    }

    public int getBuyOrderId() { return buyOrderId; }
    public int getSellOrderId() { return sellOrderId; }
    public int getPrice() { return price; }
    public int getQuantity() { return quantity; }

    /**
     * Returns a CSV representation of the trade: buyOrderId, sellOrderId, price, quantity.
     */
    @Override
    public String toString() {
        return buyOrderId + "," + sellOrderId + "," + price + "," + quantity;
    }
}

