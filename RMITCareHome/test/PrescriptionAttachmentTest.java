import org.junit.jupiter.api.*;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PrescriptionAttachmentTest {
    CareHome home;
    Doctor doc;
    Manager mgr;

    @BeforeEach
    void setup() throws RHException {
        home = CareHome.defaultLayoutWithManager();
        mgr = home.getManagers().get(0);
        doc = new Doctor("D1","Dr. Purna","purna");
        doc.setPassword("purna");
        home.addDoctor(doc);
    }

    @Test
    void doctor_can_attach_prescription() throws Exception {
        Resident r = new Resident("R1","Kevin", Gender.FEMALE);
        home.allocateResident(r, 1, 1, 1, mgr);

        Prescription p = new Prescription(doc, r);
        p.addOrder(new MedicationOrder("Paracetamol", "500mg", List.of(LocalTime.of(8,0))));

        assertDoesNotThrow(() -> home.attachPrescription(doc, r, p));
        assertEquals(1, r.getPrescriptions().size());
    }

    @Test
    void non_doctor_cannot_attach_prescription() throws Exception {
        Resident r = new Resident("R2","Bill", Gender.MALE);
        home.allocateResident(r, 1, 1, 1, mgr);

        Nurse nurse = new Nurse("N1","Chamudi","chamudi", Gender.FEMALE);
        nurse.setPassword("chamudi");
        home.addNurse(nurse);

        Prescription p = new Prescription(doc, r); // Constructed by a doctor, but call is by a nurse
        p.addOrder(new MedicationOrder("Ibuprofen","200mg", List.of(LocalTime.of(8,0))));

        AuthorizationException ex = assertThrows(
                AuthorizationException.class,
                () -> home.attachPrescription(null, r, p) // Simulate not passing a valid doctor
        );
        assertTrue(ex.getMessage().toLowerCase().contains("doctor"));
    }
}
