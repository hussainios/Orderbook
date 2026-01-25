import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExampleTest {

    @Test
    public void exampleTest() {
        
        // Here's an example of how to write a (very simple!) unit test using JUnit5.
        // For more info please see: https://junit.org/junit5/docs/current/user-guide/
        // Please note, this test can be modifed or deleted.

        Boolean expected = true;
        Boolean actual = true;

        assertEquals(expected, actual);
    }
}