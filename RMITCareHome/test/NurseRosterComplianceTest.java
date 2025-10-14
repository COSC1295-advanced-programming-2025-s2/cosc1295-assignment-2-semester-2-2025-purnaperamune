import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.*;

public class NurseRosterComplianceTest {

    CareHome home;
    Nurse n1, n2;
    Doctor d1;

    @BeforeEach
    void setup() {
        home = CareHome.defaultLayoutWithManager();
        n1 = new Nurse("N1", "Nora", "n1", Gender.FEMALE); n1.setPassword("x"); home.addNurse(n1);
        n2 = new Nurse("N2", "Nick", "n2", Gender.MALE);   n2.setPassword("x"); home.addNurse(n2);
        d1 = new Doctor("D1", "Doc", "d1"); d1.setPassword("x"); home.addDoctor(d1);
    }

    @Test
    void compliancePassesWhenCoverageAndDoctorSlotPresent() throws RHException {
        for (DayOfWeek day : DayOfWeek.values()) {
            home.assignNurseShift(n1, new Shift(day, LocalTime.of(8,0),  LocalTime.of(16,0)));
            home.assignNurseShift(n2, new Shift(day, LocalTime.of(14,0), LocalTime.of(22,0)));
            home.assignDoctorHour(d1, new Shift(day, LocalTime.of(10,0), LocalTime.of(11,0)));
        }
        assertDoesNotThrow(() -> home.checkCompliance());
    }


    @Test
    void duplicateNurseSameDayRejected() throws RHException {
        home.assignNurseShift(n1, new Shift(DayOfWeek.TUESDAY, LocalTime.of(8,0), LocalTime.of(16,0)));
        assertThrows(RosterException.class, () ->
                home.assignNurseShift(n1, new Shift(DayOfWeek.TUESDAY, LocalTime.of(14,0), LocalTime.of(22,0))));
    }

    @Test
    void missingCoverageFailsCompliance() throws RHException {
        // Provide only one shift on Wednesday + a doctor slot
        home.assignNurseShift(n1, new Shift(DayOfWeek.WEDNESDAY, LocalTime.of(8,0), LocalTime.of(16,0)));
        home.assignDoctorHour(d1, new Shift(DayOfWeek.WEDNESDAY, LocalTime.of(10,0), LocalTime.of(11,0)));
        // All other days empty -> overall check should fail
        assertThrows(ComplianceException.class, () -> home.checkCompliance());
    }
}
