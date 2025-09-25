import java.io.Serializable;
import java.util.*;

public class Prescription implements Serializable {
    private final Doctor doctor;
    private final String forResidentId;
    private final List<MedicationOrder> orders = new ArrayList<>();

    public Prescription(Doctor doctor, Resident r) {
        this.doctor = doctor; this.forResidentId = r.getId();
    }

    public void addOrder(MedicationOrder order) {
        orders.add(order);
    }

    public List<MedicationOrder> getOrders() {
        return orders;
    }

    @Override public String toString() {
        return "By " + doctor.getName() + ", orders=" + orders;
    }
}
