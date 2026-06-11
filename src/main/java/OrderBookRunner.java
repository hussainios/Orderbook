import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

public class OrderBookRunner {
    private final OrderBook book;
    private final OrderOutputter outputter;
    private final boolean printPerEventOutput;

    public OrderBookRunner(OrderBook book, OrderOutputter outputter, boolean printPerEventOutput) {
        this.book = book;
        this.outputter = outputter;
        this.printPerEventOutput = printPerEventOutput;
    }

    public ReplayStats run(Reader reader) throws IOException {
        ReplayStats stats = new ReplayStats();
        long startNanos = System.nanoTime();

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stats.incrementTotalLinesRead();
                processLine(line, stats);
            }
        }

        stats.setElapsedNanos(System.nanoTime() - startNanos);
        return stats;
    }

    private void processLine(String line, ReplayStats stats) {
        BookCommand command = InputParser.parseLine(line);
        if (command instanceof AddCommand) {
            handleAdd((AddCommand) command, stats);
        } else if (command instanceof CancelCommand) {
            handleCancel((CancelCommand) command, stats);
        }
    }

    private void handleAdd(AddCommand command, ReplayStats stats) {
        Order order = command.getOrder();
        if (book.hasLiveOrder(order.getId())) {
            return;
        }

        List<Trade> trades = book.addOrder(order);
        stats.recordAcceptedAdd(trades.size());
        if (printPerEventOutput) {
            outputter.printTrades(trades);
            outputter.printBook(book.getBuyRows(), book.getSellRows());
        }
    }

    private void handleCancel(CancelCommand command, ReplayStats stats) {
        boolean cancelled = book.cancelOrder(command.getOrderId());
        if (!cancelled) {
            return;
        }

        stats.recordAcceptedCancel();
        if (printPerEventOutput) {
            outputter.printBook(book.getBuyRows(), book.getSellRows());
        }
    }
}
