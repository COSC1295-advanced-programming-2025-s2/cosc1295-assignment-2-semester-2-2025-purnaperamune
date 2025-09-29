import java.io.Serializable;
import java.util.*;

public class Resident implements Serializable {
    private final String id;
    private final String name;
    private final Gender gender;

    private final List<Prescription> prescriptions = new ArrayList<>();
    private final List<MedicationAdministration> administrations = new ArrayList<>();

    public Resident(String id, String name, Gender gender) {
        this.id = id; this.name = name; this.gender = gender;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public Gender getGender() { return gender; }
    public List<Prescription> getPrescriptions() { return prescriptions; }
    public List<MedicationAdministration> getAdministrations() { return administrations; }

    public void addPrescription(Prescription p) { prescriptions.add(p); }
    public void addAdministration(MedicationAdministration a) { administrations.add(a); }

    @Override public boolean equals(Object o) { return (o instanceof Resident r) && r.id.equals(id); }
    @Override public int hashCode() { return Objects.hash(id); }
    @Override public String toString() { return name + " (" + gender + ", id=" + id + ")"; }
}
