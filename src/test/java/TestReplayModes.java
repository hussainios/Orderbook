import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestReplayModes {
    @Test
    public void testInputFlagMatchesStdinOutput() throws IOException {
        String input = ""
            + "ADD,B,1,100,10\n"
            + "ADD,S,2,100,4\n"
            + "CANCEL,1\n";

        Path tempFile = Files.createTempFile("orderbook-replay", ".csv");
        Files.writeString(tempFile, input, StandardCharsets.UTF_8);

        String stdinOutput = runMainWithStdin(input);
        String fileOutput = runMain(new String[] {"--input", tempFile.toString()}, null);

        assertEquals(stdinOutput, fileOutput);
    }

    @Test
    public void testBenchmarkModePrintsSummaryOnly() throws IOException {
        String input = ""
            + "ADD,B,1,100,10\n"
            + "ADD,S,2,100,4\n"
            + "CANCEL,1\n";

        String output = runMain(new String[] {"--benchmark"}, input);

        assertTrue(output.contains("totalLinesRead=3"));
        assertTrue(output.contains("acceptedCommands=3"));
        assertTrue(output.contains("acceptedAdds=2"));
        assertTrue(output.contains("acceptedCancels=1"));
        assertTrue(output.contains("ignoredCommands=0"));
        assertTrue(output.contains("tradeCount=1"));
        assertTrue(output.contains("commandsPerSecond="));
        assertTrue(output.contains("p50LatencyNanos="));
        assertTrue(output.contains("p50AddLatencyNanos="));
        assertTrue(output.contains("p50CancelLatencyNanos="));
        assertTrue(output.contains("p95LatencyNanos="));
        assertTrue(output.contains("p99LatencyNanos="));
        assertFalse(output.contains("| BUY"));
        assertFalse(output.contains("1,2,100,4"));
    }

    @Test
    public void testBenchmarkModeExcludesIgnoredCommandsFromAcceptedCounts() throws IOException {
        String input = ""
            + "ADD,B,1,100,10\n"
            + "ADD,B,1,100,5\n"
            + "CANCEL,999\n"
            + "CANCEL,1\n";

        String output = runMain(new String[] {"--benchmark"}, input);

        assertTrue(output.contains("totalLinesRead=4"));
        assertTrue(output.contains("acceptedCommands=2"));
        assertTrue(output.contains("acceptedAdds=1"));
        assertTrue(output.contains("acceptedCancels=1"));
        assertTrue(output.contains("ignoredCommands=2"));
        assertTrue(output.contains("tradeCount=0"));
    }

    @Test
    public void testBenchmarkModeCountsInvalidCommandsAsIgnoredButNotComments() throws IOException {
        String input = ""
            + "\n"
            + "# comment\n"
            + "ADD,B,1,100,10\n"
            + "BAD,ROW\n"
            + "CANCEL,missing\n"
            + "CANCEL,1\n";

        String output = runMain(new String[] {"--benchmark"}, input);

        assertTrue(output.contains("totalLinesRead=6"));
        assertTrue(output.contains("acceptedCommands=2"));
        assertTrue(output.contains("ignoredCommands=2"));
    }

    @Test
    public void testInputAndBenchmarkFlagsWorkTogether() throws IOException {
        Path workloadPath = Path.of("workloads", "canonical_replay.csv");

        String output = runMain(new String[] {"--input", workloadPath.toString(), "--benchmark"}, null);

        assertTrue(output.contains("totalLinesRead=7"));
        assertTrue(output.contains("acceptedCommands=4"));
        assertTrue(output.contains("acceptedAdds=3"));
        assertTrue(output.contains("acceptedCancels=1"));
        assertTrue(output.contains("ignoredCommands=2"));
        assertTrue(output.contains("tradeCount=1"));
    }

    @Test
    public void testCanonicalWorkloadEndToEndOutput() throws IOException {
        Path workloadPath = Path.of("workloads", "canonical_replay.csv");

        String output = runMain(new String[] {"--input", workloadPath.toString()}, null);

        String expected = ""
            + "+-----------------------------------------------------------------+\n"
            + "| BUY                            | SELL                           |\n"
            + "| Id       | Volume      | Price | Price | Volume      | Id       |\n"
            + "+----------+-------------+-------+-------+-------------+----------+\n"
            + "|         1|           10|    100|       |             |          |\n"
            + "+-----------------------------------------------------------------+\n"
            + "+-----------------------------------------------------------------+\n"
            + "| BUY                            | SELL                           |\n"
            + "| Id       | Volume      | Price | Price | Volume      | Id       |\n"
            + "+----------+-------------+-------+-------+-------------+----------+\n"
            + "|         1|           10|    100|    101|            5|         2|\n"
            + "+-----------------------------------------------------------------+\n"
            + "1,3,100,4\n"
            + "+-----------------------------------------------------------------+\n"
            + "| BUY                            | SELL                           |\n"
            + "| Id       | Volume      | Price | Price | Volume      | Id       |\n"
            + "+----------+-------------+-------+-------+-------------+----------+\n"
            + "|         1|            6|    100|    101|            5|         2|\n"
            + "+-----------------------------------------------------------------+\n"
            + "+-----------------------------------------------------------------+\n"
            + "| BUY                            | SELL                           |\n"
            + "| Id       | Volume      | Price | Price | Volume      | Id       |\n"
            + "+----------+-------------+-------+-------+-------------+----------+\n"
            + "|         1|            6|    100|       |             |          |\n"
            + "+-----------------------------------------------------------------+";

        assertEquals(expected, output);
    }

    private static String runMainWithStdin(String input) throws IOException {
        return runMain(new String[0], input);
    }

    private static String runMain(String[] args, String input) throws IOException {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;

        try (
            ByteArrayInputStream newIn = new ByteArrayInputStream((input == null ? "" : input).getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, true, StandardCharsets.UTF_8.name())
        ) {
            if (input != null) {
                System.setIn(newIn);
            }
            System.setOut(ps);

            SETSOrderBookExercise.main(args);

            return baos.toString(StandardCharsets.UTF_8.name()).trim().replaceAll("\\r\\n?", "\n");
        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }
}
