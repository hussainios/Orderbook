import java.util.Scanner;
import java.util.List;

public class SETSOrderBookExercise {

    public static void main(String[] args) {
        OrderBook book = new OrderBook();
        OrderOutputter outputter = new OrderOutputter();
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Order order = InputParser.parseLine(line);
            if (order != null) {
                List<Trade> trades = book.addOrder(order);
                outputter.printTrades(trades);
                outputter.printBook(book.getBuyRows(), book.getSellRows());
            }
        }
        scanner.close();
    }
}
