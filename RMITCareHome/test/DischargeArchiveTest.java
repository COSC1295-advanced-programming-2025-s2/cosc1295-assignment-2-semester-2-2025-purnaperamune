import org.junit.jupiter.api.*;
import java.io.File;
import static org.junit.jupiter.api.Assertions.*;

public class DischargeArchiveTest {
    CareHome home;
    Manager mgr;

    @BeforeEach
    void setup() {
        home = CareHome.defaultLayoutWithManager();
        mgr = home.getManagers().get(0);
    }

    @Test
    void discharge_creates_archive_file_and_vacates_bed() throws Exception {
        Resident r = new Resident("R1","Glen", Gender.MALE);
        home.allocateResident(r, 1, 1, 1, mgr);
        Bed b = home.getBed(1,1,1);
        assertNotNull(b.getResident());

        home.dischargeResident(r, mgr);

        // Resident removed from bed
        assertNull(b.getResident());

        // Archive file exists under "archive"
        File f = new File("archive", r.getId() + ".dat");
        assertTrue(f.exists(), "Archive file should exist");

        // cleanup
        f.delete();
        // Optionally: delete "archive" dir if empty
        new File("archive").delete();
    }
}
