import java.io.Serializable;
import java.util.*;

public class Room implements Serializable {
    private final String name;
    private final List<Bed> beds = new ArrayList<>();

    private Room(String name) { this.name = name; }

    public static Room withBeds(String name, int count) {
        Room r = new Room(name);
        for (int i = 0; i < count; i++) r.beds.add(new Bed(name + "-Bed-" + (i+1)));
        return r;
    }

    public Bed getBed(int idx) {
        if (idx < 0 || idx >= beds.size()) throw new IndexOutOfBoundsException("Invalid bed idx");
        return beds.get(idx);
    }

    public List<Bed> getBeds() { return beds; }

    /**
     * - If room empty, okay.
     * - Else, must match gender of existing residents.
     */
    public boolean isCompatibleGender(Gender g) {
        Gender existing = null;
        for (Bed b : beds) {
            if (b.getResident() != null) {
                existing = b.getResident().getGender();
                break;
            }
        }
        return existing == null || existing == g;
    }

    @Override public String toString() { return name; }
}
