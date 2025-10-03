import java.io.Serializable;

public class Bed implements Serializable {
    private final String label;
    private Resident resident;

    public Bed(String label) {
        this.label = label;
    }

    public Resident getResident() {
        return resident;
    }

    public void assignResident(Resident r) {
        this.resident = r;
    }

    public void removeResident() {
        this.resident = null;
    }

    public String simpleLabel() {
        return label;
    }

    public String describe() {
        StringBuilder sb = new StringBuilder("[" + label + "]\n");
        if (resident == null) sb.append("Vacant\n");
        else {
            sb.append("Resident: ").append(resident).append("\n");
            if (!resident.getPrescriptions().isEmpty()) {
                sb.append("Prescriptions:\n");
                for (Prescription p : resident.getPrescriptions()) sb.append("  - ").append(p).append("\n");
            }
            if (!resident.getAdministrations().isEmpty()) {
                sb.append("Administration Log:\n");
                for (MedicationAdministration a : resident.getAdministrations()) sb.append("  - ").append(a).append("\n");
            }
        }
        return sb.toString();
    }
}