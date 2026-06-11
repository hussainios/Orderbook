public class CancelCommand implements BookCommand {
    private final int orderId;

    public CancelCommand(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }
}
