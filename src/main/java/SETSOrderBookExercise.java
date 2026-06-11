import java.util.Scanner;
import java.util.List;

/**
 * Main entry point for the order book
 * Reads orders from the input, calculates the trades and displays the updated order book state.
 */
public class SETSOrderBookExercise {

    public static void main(String[] args) {
        OrderBook book = new OrderBook();
        OrderOutputter outputter = new OrderOutputter();
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            BookCommand command = InputParser.parseLine(line);
            if (command instanceof AddCommand) {
                Order order = ((AddCommand) command).getOrder();
                if (!book.hasLiveOrder(order.getId())) {
                    List<Trade> trades = book.addOrder(order);
                    outputter.printTrades(trades);
                    outputter.printBook(book.getBuyRows(), book.getSellRows());
                }
            } else if (command instanceof CancelCommand) {
                boolean cancelled = book.cancelOrder(((CancelCommand) command).getOrderId());
                if (cancelled) {
                    outputter.printBook(book.getBuyRows(), book.getSellRows());
                }
            }
        }
        scanner.close();
    }
}
