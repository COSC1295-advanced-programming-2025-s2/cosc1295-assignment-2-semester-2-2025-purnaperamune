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
        Doctor d = new Doctor("D1","Dr. Alice","alice");
        d.setPassword("pwd");
        home.addDoctor(d);

        Nurse n = new Nurse("N1","Nurse Bob","bob", Gender.MALE);
        n.setPassword("123");
        home.addNurse(n);

        Staff s1 = home.authenticate("alice","pwd");
        Staff s2 = home.authenticate("bob","123");

        assertTrue(s1 instanceof Doctor);
        assertTrue(s2 instanceof Nurse);
    }

    @Test
    void wrong_password_fails() throws RHException {
        Doctor d = new Doctor("D2","Dr. Wrong","dw");
        d.setPassword("right");
        home.addDoctor(d);
        assertNull(home.authenticate("dw","wrong"));
    }
}
