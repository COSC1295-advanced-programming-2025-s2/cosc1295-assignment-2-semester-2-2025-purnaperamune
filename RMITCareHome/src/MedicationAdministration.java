import java.io.Serializable;
import java.time.LocalDateTime;

public class MedicationAdministration implements Serializable {
    private final String medication;
    private final String dose;
    private final LocalDateTime when;
    private final Nurse by;

    public MedicationAdministration(String medication, String dose, LocalDateTime when, Nurse by) {
        this.medication = medication; this.dose = dose; this.when = when; this.by = by;
    }

    @Override public String toString() {
        return when + " :: " + medication + " " + dose + " by " + by.getName();
    }
}
