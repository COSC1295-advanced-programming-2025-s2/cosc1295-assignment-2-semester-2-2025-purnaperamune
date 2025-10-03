import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class StaffAuthTest {

    CareHome home;

    @BeforeEach
    void setup() {
        home = CareHome.defaultLayoutWithManager();
    }

    @Test
    void manager_auth_ok() {
        assertTrue(home.authenticateManager("admin", "admin"));
    }

    @Test
    void any_staff_auth_via_authenticate() throws RHException {
        Doctor d = new Doctor("D1","Dr. Purna","purna");
        d.setPassword("purna");
        home.addDoctor(d);

        Nurse n = new Nurse("Chamudi","Chamudi Abeysinghe","chamudi", Gender.FEMALE);
        n.setPassword("chamudi");
        home.addNurse(n);

        Staff s1 = home.authenticate("purna","purna");
        Staff s2 = home.authenticate("chamudi","chamudi");

        assertTrue(s1 instanceof Doctor);
        assertTrue(s2 instanceof Nurse);
    }

    @Test
    void wrong_password_fails() throws RHException {
        Doctor d = new Doctor("D1","Dr. Purna","purna");
        d.setPassword("purna");
        home.addDoctor(d);
        assertNull(home.authenticate("purna","purna123"));
    }
}
