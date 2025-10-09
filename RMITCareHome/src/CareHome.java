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
        h.wards.add(Ward.of("Ward-1", new int[]{1,2,2,3,4,2}));
        // Ward 1
        h.wards.add(Ward.of("Ward-2", new int[]{1,2,3,2,4,2}));
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

    public void assignDoctorHour(Doctor d, Shift hourSlot) throws RHException {
        // 1-hour only
        if (Duration.between(hourSlot.start(), hourSlot.end()).toHours() != 1)
            throw new ValidationException("Doctor slot must be 1 hour.");
        doctorRoster.get(hourSlot.day()).add(new ShiftAssignment<>(d, hourSlot));
    }

    // Beds & Residents related methods
    public Bed getBed(int wardIdx, int roomIdx, int bedIdx) throws RHException {
        // Validate 1-based then convert to 0-based
        if (wardIdx <= 0 || wardIdx > wards.size())
            throw new ValidationException("Invalid ward number (use 1.." + wards.size() + ").");

        Ward w = wards.get(wardIdx - 1);
        int roomCount = w.getRooms().size();
        if (roomIdx <= 0 || roomIdx > roomCount)
            throw new ValidationException("Invalid room number for ward " + wardIdx + " (use 1.." + roomCount + ").");

        Room r = w.getRooms().get(roomIdx - 1);
        int bedCount = r.getBeds().size();
        if (bedIdx <= 0 || bedIdx > bedCount)
            throw new ValidationException("Invalid bed number for ward " + wardIdx + ", room " + roomIdx +
                    " (use 1.." + bedCount + ").");

        return r.getBeds().get(bedIdx - 1);
    }

    public void allocateResident(Resident r, int wardIdx, int roomIdx, int bedIdx, Manager by) throws RHException {
        Bed b = getBed(wardIdx, roomIdx, bedIdx);
        if (b.getResident() != null) throw new BedOccupiedException("Bed already occupied.");
        // if room has other residents, ensure same gender.
        // FIX: 1-based -> 0-based for direct access to ward/room
        Room room = wards.get(wardIdx - 1).getRoom(roomIdx - 1);
        if (!room.isCompatibleGender(r.getGender()))
            throw new ValidationException("Gender incompatibility for room.");
        b.assignResident(r);
        logs.add(ActionLog.now(ActionType.ADD_RESIDENT, by.getId(), "Alloc " + r.getName() + " -> " + b.simpleLabel()));
    }

    public void moveResident(int fromW, int fromR, int fromB, int toW, int toR, int toB, Manager by) throws RHException {
        Bed from = getBed(fromW, fromR, fromB);
        Bed to = getBed(toW, toR, toB);
        Resident r = from.getResident();
        if (r == null) throw new ValidationException("Source bed empty.");
        if (to.getResident() != null) throw new BedOccupiedException("Destination bed occupied.");
        // gender check on target room
        // FIX: 1-based -> 0-based for direct access to ward/room
        if (!wards.get(toW - 1).getRoom(toR - 1).isCompatibleGender(r.getGender()))
            throw new ValidationException("Gender incompatibility in target room.");
        from.removeResident();
        to.assignResident(r);
        logs.add(ActionLog.now(ActionType.MOVE_RESIDENT, by.getId(), "Move " + r.getName() + " -> " + to.simpleLabel()));
    }

    // Prescription related methods
    public void attachPrescription(Doctor doctor, Resident r, Prescription p) throws RHException {
        if (doctor == null) throw new AuthorizationException("Only doctors can attach prescriptions.");
        if (!doctors.contains(doctor)) throw new AuthorizationException("Unknown doctor.");
        r.addPrescription(p);
        logs.add(ActionLog.now(ActionType.ADD_PRESCRIPTION, doctor.getId(), "Rx for " + r.getName()));
    }

    public void recordAdministration(Nurse nurse, Resident r, String med, String dose, LocalDateTime when) throws RHException {
        if (nurse == null) throw new AuthorizationException("Only nurses can administer.");
        if (!nurses.contains(nurse)) throw new AuthorizationException("Unknown nurse.");

        DayOfWeek d = when.getDayOfWeek();
        boolean rostered = nurseRoster.get(d).stream().anyMatch(a -> a.staff().equals(nurse));
        if (!rostered) throw new NotRosteredException("Nurse not rostered today.");
        r.addAdministration(new MedicationAdministration(med, dose, when, nurse));
        logs.add(ActionLog.now(ActionType.ADMINISTER_MED, nurse.getId(), "Admin " + med + " to " + r.getName()));
    }

    public void dischargeResident(Resident r, Manager by) throws RHException, IOException {
        // remove from bed
        Bed b = findBedOf(r);
        if (b == null) throw new ValidationException("Resident not in any bed.");
        b.removeResident();

        File dir = new File("archive");
        if (!dir.exists()) dir.mkdirs();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(dir, r.getId() + ".dat")))) {
            oos.writeObject(r);
        }
        logs.add(ActionLog.now(ActionType.DISCHARGE, by.getId(), "Discharge " + r.getName()));
    }

    private Bed findBedOf(Resident r) {
        for (Ward w: wards) {
            for (Room rm: w.getRooms()) {
                for (Bed b: rm.getBeds()) {
                    if (b.getResident() != null && b.getResident().equals(r)) return b;
                }
            }
        }
        return null;
    }

    // -------- NEW: Readable layout snapshot
    /** Returns a readable snapshot of all wards/rooms/beds and occupancy (1-based labels). */
    public String layoutSnapshot() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========= Current Bed Layout =========\n");
        for (int w = 0; w < wards.size(); w++) {
            sb.append("Ward ").append(w + 1).append(":\n");
            List<Room> rooms = wards.get(w).getRooms();
            for (int r = 0; r < rooms.size(); r++) {
                Room room = rooms.get(r);
                sb.append("  Room ").append(r + 1).append(": ");
                List<Bed> beds = room.getBeds();
                for (int b = 0; b < beds.size(); b++) {
                    Bed bed = beds.get(b);
                    String occ = (bed.getResident() == null)
                            ? "[Bed " + (b + 1) + ": VACANT]"
                            : "[Bed " + (b + 1) + ": " + bed.getResident().getName() + "]";
                    sb.append(occ).append("  ");
                }
                sb.append("\n");
            }
        }
        sb.append("======================================\n");
        return sb.toString();
    }

    // -------- Compliance check
    // - Each day: at least one nurse on 08-16 and at least one nurse on 14-22
    // - No nurse > 8h per day (enforced at assignment)
    // - At least one doctor 1-hr slot each day
    public void checkCompliance() throws RHException {
        for (DayOfWeek d: DayOfWeek.values()) {
            List<ShiftAssignment<Nurse>> nDay = nurseRoster.get(d);
            boolean has0816 = nDay.stream().anyMatch(a -> a.shift().start().equals(LocalTime.of(8,0)) && a.shift().end().equals(LocalTime.of(16,0)));
            boolean has1422 = nDay.stream().anyMatch(a -> a.shift().start().equals(LocalTime.of(14,0)) && a.shift().end().equals(LocalTime.of(22,0)));
            if (!(has0816 && has1422))
                throw new ComplianceException("Nurse coverage missing for " + d + " (need 08-16 and 14-22).");

            Map<Nurse, Long> count = new HashMap<>();
            for (ShiftAssignment<Nurse> a : nDay) count.merge(a.staff(), 1L, Long::sum);
            for (Map.Entry<Nurse, Long> e : count.entrySet())
                if (e.getValue() > 1) throw new ComplianceException("Nurse " + e.getKey().getName() + " assigned >1 shift on " + d);
            // doctors
            List<ShiftAssignment<Doctor>> dDay = doctorRoster.get(d);
            boolean hasDoctorHour = dDay.stream().anyMatch(a -> Duration.between(a.shift().start(), a.shift().end()).toHours() == 1);
            if (!hasDoctorHour) throw new ComplianceException("No doctor 1-hour slot set for " + d);
        }
    }

    public void save(String file) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(this);
        }
    }

    public static CareHome load(String file) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (CareHome) ois.readObject();
        }
    }
}
