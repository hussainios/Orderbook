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
        if (command == null) {
            if (isIgnoredCommandLine(line)) {
                stats.recordIgnoredCommand();
            }
            return;
        }

        if (command instanceof AddCommand) {
            handleAdd((AddCommand) command, stats);
        } else if (command instanceof CancelCommand) {
            handleCancel((CancelCommand) command, stats);
        }
    }

    private void handleAdd(AddCommand command, ReplayStats stats) {
        Order order = command.getOrder();
        if (book.hasLiveOrder(order.getId())) {
            stats.recordIgnoredCommand();
            return;
        }

        long startNanos = System.nanoTime();
        List<Trade> trades = book.addOrder(order);
        long latencyNanos = System.nanoTime() - startNanos;
        stats.recordAcceptedAdd(trades.size(), latencyNanos);
        if (printPerEventOutput) {
            outputter.printTrades(trades);
            outputter.printBook(book.getBuyRows(), book.getSellRows());
        }
    }

    private void handleCancel(CancelCommand command, ReplayStats stats) {
        long startNanos = System.nanoTime();
        boolean cancelled = book.cancelOrder(command.getOrderId());
        if (!cancelled) {
            stats.recordIgnoredCommand();
            return;
        }

        long latencyNanos = System.nanoTime() - startNanos;
        stats.recordAcceptedCancel(latencyNanos);
        if (printPerEventOutput) {
            outputter.printBook(book.getBuyRows(), book.getSellRows());
        }
    }

    private boolean isIgnoredCommandLine(String line) {
        if (line == null) {
            return false;
        }

        String trimmed = line.trim();
        return !trimmed.isEmpty() && !trimmed.startsWith("#");
    }
}
