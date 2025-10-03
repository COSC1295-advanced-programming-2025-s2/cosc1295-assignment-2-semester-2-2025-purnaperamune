import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class RoomGenderCompatibilityTest {
    CareHome home;

    @BeforeEach
    void setup() {
        home = CareHome.defaultLayoutWithManager();
    }

    @Test
    void cannot_mix_genders_in_same_room() throws Exception {
        Manager mgr = home.getManagers().get(0);
        Resident r1 = new Resident("R1","Maxwell", Gender.MALE);
        Resident r2 = new Resident("R2","Rose", Gender.FEMALE);

        // Assigning Maxwell (Male) to Ward - 1 Room - 1 Bed - 1
        home.allocateResident(r1, 1, 1, 1, mgr);

        // Attempting to assign Rose (Female) in another bed of the same room - (Should fail)
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> home.allocateResident(r2, 1, 1, 2, mgr)
        );
        assertFalse(ex.getMessage().toLowerCase().contains("gender"));
    }
}
