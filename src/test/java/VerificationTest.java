
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.stream.Stream;
import java.io.IOException;

public class VerificationTest {
  
    @ParameterizedTest
    @MethodSource("verificationTestsProvider")
    public void runTest(String testId, String input, String expectedOutput) throws IOException {

        String stdOut = getOrderBookOutput(input);
        System.out.println(stdOut);

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

    static Stream<Arguments> verificationTestsProvider() {
        return Stream.of(
            Arguments.of("NormalOrder", 
                ""
                .concat("B,1,1,1\n")
                .concat("S,2,2,1"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|            1|      1|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|            1|      1|      2|            1|         2|\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            Arguments.of("OrderWithComment", 
                ""
                .concat("\n")
                .concat("# Comment\n")
                .concat(" # Another valid comment\n")
                .concat("B,1,1,1\n")
                .concat("S,2,2,1"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|            1|      1|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|            1|      1|      2|            1|         2|\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            Arguments.of("OrderIdFormat", 
                ""
                .concat("B,123456789,1,1\n")
                .concat("S,123456780,2,1"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("| 123456789|            1|      1|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("| 123456789|            1|      1|      2|            1| 123456780|\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            Arguments.of("OrderPriceFormat", 
                ""
                .concat("B,1,12345,1\n")
                .concat("S,2,12346,1"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|            1| 12,345|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|            1| 12,345| 12,346|            1|         2|\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            Arguments.of("OrderVolumeFormat", 
                ""
                .concat("S,1,2,1234567890\n")
                .concat("B,2,1,1234567890"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|          |             |       |      2|1,234,567,890|         1|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         2|1,234,567,890|      1|      2|1,234,567,890|         1|\n")
                .concat("+-----------------------------------------------------------------+")
            ),
            Arguments.of("SingleTrade", 
                ""
                .concat("B,1,1,2\n")
                .concat("S,2,2,1\n")
                .concat("S,3,1,1"),
                ""
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|            2|      1|       |             |          |\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|            2|      1|      2|            1|         2|\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("1,3,1,1\n")
                .concat("+-----------------------------------------------------------------+\n")
                .concat("| BUY                            | SELL                           |\n")
                .concat("| Id       | Volume      | Price | Price | Volume      | Id       |\n")
                .concat("+----------+-------------+-------+-------+-------------+----------+\n")
                .concat("|         1|            1|      1|      2|            1|         2|\n")
                .concat("+-----------------------------------------------------------------+")
            )
        );
    }
}