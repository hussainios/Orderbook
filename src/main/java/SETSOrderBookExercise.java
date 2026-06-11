import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Main entry point for the order book
 * Reads orders from the input, calculates the trades and displays the updated order book state.
 */
public class SETSOrderBookExercise {

    private static final class RunOptions {
        private final String inputPath;
        private final boolean benchmarkMode;

        private RunOptions(String inputPath, boolean benchmarkMode) {
            this.inputPath = inputPath;
            this.benchmarkMode = benchmarkMode;
        }
    }

    public static void main(String[] args) throws IOException {
        RunOptions options = parseArgs(args);
        OrderBook book = new OrderBook();
        OrderOutputter outputter = new OrderOutputter();
        OrderBookRunner runner = new OrderBookRunner(book, outputter, !options.benchmarkMode);

        try (Reader reader = openReader(options.inputPath)) {
            ReplayStats stats = runner.run(reader);
            if (options.benchmarkMode) {
                System.out.println(stats.toSummaryString());
            }
        }
    }

    private static RunOptions parseArgs(String[] args) {
        String inputPath = null;
        boolean benchmarkMode = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--benchmark".equals(arg)) {
                benchmarkMode = true;
            } else if ("--input".equals(arg)) {
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for --input");
                }
                inputPath = args[++i];
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        return new RunOptions(inputPath, benchmarkMode);
    }

    private static Reader openReader(String inputPath) throws IOException {
        if (inputPath == null) {
            return new InputStreamReader(System.in);
        }
        return Files.newBufferedReader(Path.of(inputPath));
    }
}
