import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class BedIndexingAndValidationTest {

    CareHome home;

    @BeforeEach
    void setup() {
        home = CareHome.defaultLayoutWithManager();
    }

    @Test
    void getBedRejectsZeroOrNegative() {
        assertThrows(ValidationException.class, () -> home.getBed(0, 1, 1));
        assertThrows(ValidationException.class, () -> home.getBed(1, 0, 1));
        assertThrows(ValidationException.class, () -> home.getBed(1, 1, 0));
    }

    @Test
    void getBedRejectsOutOfRange() {
        // Ward 3 doesn't exist in default (only 2)
        assertThrows(ValidationException.class, () -> home.getBed(3, 1, 1));
        // Room 99 likely out of range
        assertThrows(ValidationException.class, () -> home.getBed(1, 99, 1));
    }

    @Test
    void getBedValid1Based() throws RHException {
        Bed b = home.getBed(1, 1, 1); // first ward, first room, first bed
        assertNotNull(b);
    }
}
