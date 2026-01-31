
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


/**
 * End 2 End Tests for the Iceberg orders.
 */

public class TestIceberg {
  
    @ParameterizedTest
    @MethodSource("icebergTestsProvider")
    public void runTest(String testId, String input, String expectedOutput) throws IOException {

        String stdOut = getOrderBookOutput(input);

        assertEquals(expectedOutput, stdOut);
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

    static Stream<Arguments> icebergTestsProvider() {
        return Stream.of(
            Arguments.of("IcebergExecution", 
                ""
                .concat("B,1,100,50,20\n")
                .concat("S,2,100,20\n")
                .concat("S,3,100,10"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           20|    100|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("1,2,100,20\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           20|    100|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("1,3,100,10\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           10|    100|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            Arguments.of("IcebergPriorityLoss", 
                ""
                .concat("B,1,100,50,20\n")
                .concat("B,2,100,10\n")
                .concat("S,3,100,20\n")
                .concat("S,4,100,10"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           20|    100|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           20|    100|       |             |          |\n")
                .concat("|         2|           10|    100|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("1,3,100,20\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         2|           10|    100|       |             |          |\n")
                .concat("|         1|           20|    100|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("2,4,100,10\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|           20|    100|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+")
            )
        );
    }
}
