import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestOrderOutputter {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setUp() {
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void restore() {
        System.setOut(originalOut);
    }

    @Test
    public void testPrintTrades() {
        List<Trade> trades = Arrays.asList(
            new Trade(1, 2, 100, 50),
            new Trade(3, 4, 105, 20)
        );
        
        OrderOutputter orderOutputter = new OrderOutputter();
        orderOutputter.printTrades(trades);

        // System.out.println adds a line separator, so we expect that in the output
        String expected = "1,2,100,50" + System.lineSeparator() + 
                          "3,4,105,20" + System.lineSeparator();
        
        assertEquals(expected, outContent.toString());
    }


    @Test
    public void testPrintBook() {
        List<BookRow> buyRows = Arrays.asList(
            new BookRow(1, 1000, 99),
            new BookRow(3, 2000, 98)
        );
        List<BookRow> sellRows = Arrays.asList(
            new BookRow(2, 500, 101)
        );

        OrderOutputter orderOutputter = new OrderOutputter();
        orderOutputter.printBook(buyRows, sellRows);

        // Construct the expected table output
        String expected = "+-----------------------------------------------------------------+" + System.lineSeparator() +
                          "| BUY                            | SELL                           |" + System.lineSeparator() +
                          "| Id       | Volume      | Price | Price | Volume      | Id       |" + System.lineSeparator() +
                          "+----------+-------------+-------+-------+-------------+----------+" + System.lineSeparator() +
                          "|         1|        1,000|     99|    101|          500|         2|" + System.lineSeparator() +
                          "|         3|        2,000|     98|       |             |          |" + System.lineSeparator() +
                          "+-----------------------------------------------------------------+" + System.lineSeparator();
        
        assertEquals(expected, outContent.toString());
    }
}