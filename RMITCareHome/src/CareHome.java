import java.io.*;
import java.time.*;
import java.util.*;

public class CareHome implements Serializable {
    private final List<Ward> wards = new ArrayList<>();
    private final List<Nurse> nurses = new ArrayList<>();
    private final List<Doctor> doctors = new ArrayList<>();
    private final List<Manager> managers = new ArrayList<>();
    private final List<ActionLog> logs = new ArrayList<>();

    // Roster: for each day, which nurses cover 8-16 and 14-22; doctors: one 1-hr slot/day
    private final Map<DayOfWeek, List<ShiftAssignment<Nurse>>> nurseRoster = new EnumMap<>(DayOfWeek.class);
    private final Map<DayOfWeek, List<ShiftAssignment<Doctor>>> doctorRoster = new EnumMap<>(DayOfWeek.class);

    public CareHome() {
        for (DayOfWeek d : DayOfWeek.values()) {
            nurseRoster.put(d, new ArrayList<>());
            doctorRoster.put(d, new ArrayList<>());
        }
    }

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

    public void addNurse(Nurse n) { nurses.add(n); }
    public void addDoctor(Doctor d) { doctors.add(d); }
    public List<Manager> getManagers() { return managers; }
    public Staff findStaffByUsername(String u) {
        for (Manager m: managers) if (m.getUsername().equals(u)) return m;
        for (Doctor d: doctors) if (d.getUsername().equals(u)) return d;
        for (Nurse n: nurses) if (n.getUsername().equals(u)) return n;
        return null;
    }

    public void assignNurseShift(Nurse n, Shift s) throws RHException {
        // rule: nurse cannot exceed 8h/day (we stop double-booking on same day)
        if (Duration.between(s.start(), s.end()).toHours() != 8)
            throw new ValidationException("Nurse shift must be exactly 8 hours (08-16 or 14-22).");
        // prevent >1 shift on same day
        List<ShiftAssignment<Nurse>> day = nurseRoster.get(s.day());
        for (ShiftAssignment<Nurse> asg : day) {
            if (asg.staff().equals(n) && asg.shift().day() == s.day())
                throw new RosterException("Nurse already assigned on " + s.day());
        }
        day.add(new ShiftAssignment<>(n, s));
    }

}