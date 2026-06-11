public class AddCommand implements BookCommand {
    private final Order order;

    public AddCommand(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }
}
