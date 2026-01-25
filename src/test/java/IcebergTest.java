import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.stream.Stream;
import java.io.IOException;

public class IcebergTest {

    @ParameterizedTest
    @MethodSource("icebergTestsProvider")
    public void runTest(String testId, String input, String expectedOutput) throws IOException {
        String stdOut = getOrderBookOutput(input);
        assertEquals(expectedOutput, stdOut, "Failed test case: " + testId);
    }

    private static String getOrderBookOutput(String input) throws IOException {
        InputStream oldIn = System.in;
        PrintStream oldOut = System.out;

        try (
            ByteArrayInputStream newIn = new ByteArrayInputStream(input.getBytes());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos, true)
        ){
            System.setIn(newIn);
            System.setOut(ps);

            SETSOrderBookExercise.main(new String[0]);

            return baos.toString(StandardCharsets.UTF_8.name()).trim().replaceAll("\\r\\n?", "\n");

        } finally {
            System.setIn(oldIn);
            System.setOut(oldOut);
        }
    }

    static Stream<Arguments> icebergTestsProvider1() {
        return Stream.of(
            // Requirement (Instructions 64-68): Single trade message for multiple peaks
            // Scenario: Buy Iceberg 100,000 @ 5103 (Peak 10,000)
            // Match with Sell 16,000 @ 5103
            // Result: ONE trade message for 16,000, and book shows 4,000 remaining in current peak.
            Arguments.of("SingleTradeForMultiplePeaks",
                ""
                .concat("B,100322,5103,100000,10000\n")
                .concat("S,100345,5103,16000"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|    100322|       10,000|  5,103|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("100322,100345,5103,16000\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                // Note: 16,000 traded. 10,000 (peak 1) + 6,000 (peak 2). 
                // Remaining in peak 2 is 4,000.
                .concat("|    100322|        4,000|  5,103|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+")
            )
        );
    }
    static Stream<Arguments> icebergTestsProvider() {
        return Stream.of(
            // Existing test: Single trade message for multiple peaks
            Arguments.of("SingleTradeForMultiplePeaks",
                ""
                .concat("B,100322,5103,100000,10000\n")
                .concat("S,100345,5103,16000"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|    100322|       10,000|  5,103|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("100322,100345,5103,16000\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|    100322|        4,000|  5,103|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            
            // NEW TEST 1: Exact multiple of peak size - 3 complete peaks
            Arguments.of("ExactThreePeaksMatch",
                ""
                .concat("S,1,100,50,10\n")  // Sell Iceberg: 50 total, 10 peak
                .concat("B,2,100,30"),      // Buy: exactly 3 peaks
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|          |             |       |    100|           10|         1|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("2,1,100,30\n")  // Single trade for all 30 units across 3 peaks
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|          |             |       |    100|           10|         1|\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            
            // NEW TEST 2: Partial match of first peak only (baseline)
            Arguments.of("PartialFirstPeakOnly",
                ""
                .concat("B,10,200,100,20\n")  // Buy Iceberg: 100 total, 20 peak
                .concat("S,20,200,5"),        // Sell: only 5 units
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|        10|           20|    200|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("10,20,200,5\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|        10|           15|    200|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            
            // NEW TEST 3: Complete exhaustion of iceberg across all peaks
            Arguments.of("CompleteIcebergExhaustion",
                ""
                .concat("S,5,150,25,8\n")   // Sell Iceberg: 25 total, 8 peak (3 peaks + 1 unit)
                .concat("B,6,150,25"),      // Buy: all 25 units
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|          |             |       |    150|            8|         5|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("6,5,150,25\n")  // Single trade for complete iceberg
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("+-----------------------------------------------------------------+")  // Empty book
            ),
            
            // NEW TEST 4: Multiple aggressors, each generating separate trades
            // (To verify that the "single trade" rule applies per aggressor, not per iceberg)
            Arguments.of("MultipleAggressorsSeparateTrades",
                ""
                .concat("B,100,500,100,15\n")  // Buy Iceberg: 100 total, 15 peak
                .concat("S,200,500,20\n")      // First aggressor: 20 units
                .concat("S,300,500,10"),       // Second aggressor: 10 units
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|       100|           15|    500|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("100,200,500,20\n")  // Trade 1: First aggressor matches 20
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|       100|           10|    500|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("100,300,500,10\n")  // Trade 2: Second aggressor matches 10
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|       100|           15|    500|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+")
            )
        );
    }
    
}