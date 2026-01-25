/**
 * Represents an execution between a buyer and a seller.
 */
public class Trade {
    private final int buyOrderId;
    private final int sellOrderId;
    private final int price;
    private final int quantity;

    /**
     * Constructs a new Trade record.
     *
     * @param buyOrderId the ID of the buying order
     * @param sellOrderId the ID of the selling order
     * @param price the price at which the trade occurred
     * @param quantity the amount of shares traded
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
     * Returns a CSV representation of the trade: buyOrderId,sellOrderId,price,quantity.
     */
    @Override
    public String toString() {
        return buyOrderId + "," + sellOrderId + "," + price + "," + quantity;
    }
}

