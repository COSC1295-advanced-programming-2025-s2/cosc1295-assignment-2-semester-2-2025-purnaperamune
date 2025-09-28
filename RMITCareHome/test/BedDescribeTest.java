import org.junit.jupiter.api.*;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BedDescribeTest {
    CareHome home;
    Manager mgr;
    Doctor doc;
    Nurse nurse;

    @BeforeEach
    void setup() throws RHException {
        home = CareHome.defaultLayoutWithManager();
        mgr = home.getManagers().get(0);
        doc = new Doctor("D1","Dr. Smith","smith"); doc.setPassword("pw"); home.addDoctor(doc);
        nurse = new Nurse("N1","Nina","nina", Gender.FEMALE); nurse.setPassword("pw"); home.addNurse(nurse);
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        home.assignNurseShift(nurse, new Shift(today, LocalTime.of(8,0), LocalTime.of(16,0)));
    }

    @Test
    void describe_shows_resident_rx_and_admins() throws Exception {
        Resident r = new Resident("R1","Mary", Gender.FEMALE);
        home.allocateResident(r, 1, 1, 1, mgr);

        Prescription p = new Prescription(doc, r);
        p.addOrder(new MedicationOrder("Paracetamol","500mg", List.of(LocalTime.of(8,0))));
        home.attachPrescription(doc, r, p);

        home.recordAdministration(nurse, r, "Paracetamol","500mg", LocalDateTime.now());

        String desc = home.getBed(1,1,1).describe();
        assertTrue(desc.contains("Resident: Mary"));
        assertTrue(desc.contains("Paracetamol"));
        assertTrue(desc.contains("Administration"));
    }

    @Test
    void describe_vacant() throws Exception {
        String desc = home.getBed(1,1,1).describe();
        assertTrue(desc.toLowerCase().contains("vacant"));
    }
}
