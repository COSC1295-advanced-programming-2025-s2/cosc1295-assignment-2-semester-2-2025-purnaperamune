import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class GetBedValidationTest {
    CareHome home;

    @BeforeEach
    void setup() {
        home = CareHome.defaultLayoutWithManager();
    }

    @Test
    void invalid_ward_throws_validation() {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> home.getBed(99, 1, 1)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid ward"));
    }

    @Test
    void invalid_room_throws_validation() throws RHException {
        // ward 1 exists, picking an invalid room
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> home.getBed(1, 999, 1)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid room"));
    }

    @Test
    void invalid_bed_throws_validation() throws RHException {
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> home.getBed(1, 1, 999)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("invalid bed"));
    }
}
