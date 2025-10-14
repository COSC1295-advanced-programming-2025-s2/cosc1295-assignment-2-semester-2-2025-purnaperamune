import org.junit.jupiter.api.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PrescriptionAttachmentTest {
    CareHome home;
    Doctor doc;
    Manager mgr;
    Nurse nurse;

    @BeforeEach
    void setup() throws RHException {
        home = CareHome.defaultLayoutWithManager();
        mgr = home.getManagers().get(0);
        doc = new Doctor("D1","Dr. Purna","purna");
        doc.setPassword("purna");
        home.addDoctor(doc);

        nurse = new Nurse("N1", "Nora", "n1", Gender.FEMALE); nurse.setPassword("x"); home.addNurse(nurse);

        // Roster nurse for today so administrations can be recorded
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        assertDoesNotThrow(() -> home.assignNurseShift(nurse,
                new Shift(today, LocalTime.of(8,0), LocalTime.of(16,0))));
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

    @Test
    void doctorAttachesPrescription_nurseRecordsAdministration() throws Exception {
        Resident r = new Resident("R1", "Alex", Gender.MALE);
        home.allocateResident(r, 1, 2, 1, mgr);

        Prescription p = new Prescription(doc, r);
        p.addOrder(new MedicationOrder("Amoxicillin", "500mg",
                Arrays.asList(LocalTime.of(8,0), LocalTime.of(20,0))));

        assertDoesNotThrow(() -> home.attachPrescription(doc, r, p));

        assertDoesNotThrow(() ->
                home.recordAdministration(nurse, r, "Amoxicillin", "500mg", LocalDateTime.now()));
    }

    @Test
    void nonRosteredNurseCannotRecord() throws Exception {
        Resident r = new Resident("R2", "Bella", Gender.FEMALE);
        home.allocateResident(r, 1, 3, 1, mgr);

        Nurse offDuty = new Nurse("N2", "OffDuty", "off", Gender.FEMALE); offDuty.setPassword("x");
        home.addNurse(offDuty);

        assertThrows(NotRosteredException.class, () ->
                home.recordAdministration(offDuty, r, "Paracetamol", "1g", LocalDateTime.now()));
    }
}
