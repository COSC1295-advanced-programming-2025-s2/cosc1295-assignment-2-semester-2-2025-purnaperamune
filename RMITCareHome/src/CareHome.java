import java.io.*;
import java.time.*;
import java.util.*;

public class CareHome implements Serializable {
    private final List<Ward> wards = new ArrayList<>();
    private final List<Nurse> nurses = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Manager> managers = new ArrayList<>();
    private final List<ActionLog> logs = new ArrayList<>();

    // Ward layout
    public static CareHome defaultLayoutWithManager() {
        CareHome h = new CareHome();
        // Two wards with 6 rooms each, beds per room vary (1..4).
        // Ward 0
        h.wards.add(Ward.of("Ward-1", new int[]{1, 2, 4, 4, 4, 4}));
        // Ward 1
        h.wards.add(Ward.of("Ward-2", new int[]{1, 2, 4, 4, 4, 4}));
        // Default manager
        Manager m = new Manager(UUID.randomUUID().toString(), "Default Manager", "admin");
        m.setPassword("admin");
        h.managers.add(m);
        return h;
    }

    // Authentication
    public boolean authenticateManager(String username, String password) {
        for (Manager m : managers) if (Objects.equals(m.getUsername(), username) && m.checkPassword(password)) return true;
        return false;
    }

    public Staff authenticate(String username, String password) {
        for (Manager m : managers) {
            if (m.getUsername().equals(username) && m.checkPassword(password)) return m;
        }
        for (Doctor d : doctors) {
            if (d.getUsername().equals(username) && d.checkPassword(password)) return d;
        }
        for (Nurse n : nurses) {
            if (n.getUsername().equals(username) && n.checkPassword(password)) return n;
        }
        return null;
    }

}