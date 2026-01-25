import java.util.Scanner;

public class SETSOrderBookExercise {

    public static void main(String[] args) {
        OrderBook book = new OrderBook();
        Scanner scanner = new Scanner(System.in);

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Order order = InputParser.parseLine(line);
            if (order != null) {
                book.addOrder(order);
            }
        }
    }
}
