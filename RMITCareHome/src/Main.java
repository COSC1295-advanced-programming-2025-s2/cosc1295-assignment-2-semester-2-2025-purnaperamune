import java.io.IOException;
import java.time.*;
import java.util.*;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static CareHome home;

    public static void main(String[] args) {
        System.out.println("=== RMIT Care Home :: Resident HealthCare System (Phase 1, Console) ===");
        try {
            home = CareHome.load("carehome.dat");
            System.out.println("Loaded existing state from carehome.dat");
        } catch (Exception e) {
            System.out.println("No saved state found. Creating a fresh CareHome with 2 wards and default manager.");
            home = CareHome.defaultLayoutWithManager();
        }
        loginManagerAndMenu();
        System.out.println("Goodbye.");
    }

    // Menu options
    private static void printMenu() {
        System.out.println("\n=======================================");
        System.out.println("  RMIT CARE HOME MANAGEMENT SYSTEM");
        System.out.println("=======================================");
        System.out.println(" [1]  Add Staff (Doctor/Nurse)");
        System.out.println(" [2]  Update Staff Password");
        System.out.println(" [3]  Assign Nurse Shift");
        System.out.println(" [4]  Assign Doctor 1-hr Slot");
        System.out.println(" [5]  Add Resident to Vacant Bed");
        System.out.println(" [6]  Move Resident to Another Bed");
        System.out.println(" --------------------------------------");
        System.out.println(" [7]  Attach Prescription (Doctor Only)");
        System.out.println(" [8]  Record Medication Administration (Nurse Only)");
        System.out.println(" --------------------------------------");
        System.out.println(" [9]  View Bed Details");
        System.out.println(" [10] Compliance Check");
        System.out.println(" [11] Discharge Resident (Archive)");
        System.out.println(" --------------------------------------");
        System.out.println(" [12] Save Data");
        System.out.println(" [13] Load Data");
        System.out.println(" [14] List All Wards/Rooms/Beds (with occupancy)");
        System.out.println(" --------------------------------------");
        System.out.println(" [0]  Exit");
        System.out.println("=======================================");
        System.out.print("Choose an option (0-14): ");
    }

    // Manager login and main loop
    private static void loginManagerAndMenu() {
        System.out.print("Manager username: ");
        String u = sc.nextLine().trim();
        System.out.print("Manager password: ");
        String p = sc.nextLine().trim();
        if (!home.authenticateManager(u, p)) {
            System.out.println("Authentication failed.");
            return;
        }
        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addStaff();
                    case "2" -> updateStaffPass();
                    case "3" -> assignNurseShift();
                    case "4" -> assignDoctorHour();
                    case "5" -> addResidentToBed();
                    case "6" -> moveResident();
                    case "7" -> attachPrescriptionWithDoctorLogin();  // Doctor credentials required here
                    case "8" -> recordAdministrationWithNurseLogin(); // Nurse credentials required here
                    case "9" -> viewBed();
                    case "10" -> runCompliance();
                    case "11" -> dischargeResident();
                    case "12" -> { home.save("carehome.dat"); System.out.println("\nData saved successfully."); }
                    case "13" -> { home = CareHome.load("carehome.dat"); System.out.println("\nData loaded successfully."); }
                    case "14" -> System.out.println(home.layoutSnapshot());
                    case "0" -> {
                        System.out.println("Thank you for using the system!");
                        return;
                    }
                    default -> System.out.println("\nInvalid choice. Please try again.");
                }
            } catch (RHException | IOException | ClassNotFoundException ex) {
                System.out.println("\nError: " + ex.getMessage());
            }
        }
    }

    // Adding new staff
    private static void addStaff() throws RHException {
        System.out.print("Add (D)octor or (N)urse? ");
        String type = sc.nextLine().trim().toUpperCase();

        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        System.out.print("Username: ");
        String user = sc.nextLine().trim();
        System.out.print("Password: ");
        String pass = sc.nextLine().trim();

        if (type.equals("D")) {
            Doctor d = new Doctor(UUID.randomUUID().toString(), name, user);
            d.setPassword(pass);
            home.addDoctor(d);
            System.out.println("Doctor added: " + d.getName() + " - " + d.getUsername());
        } else if (type.equals("N")) {
            System.out.print("Gender (M/F): ");
            String g = sc.nextLine().trim().toUpperCase();
            Gender gender = g.startsWith("M") ? Gender.MALE : Gender.FEMALE;
            Nurse n = new Nurse(UUID.randomUUID().toString(), name, user, gender);
            n.setPassword(pass);
            home.addNurse(n);
            System.out.println("Nurse added: " + n + " - " + n.getUsername());
        } else {
            System.out.println("Cancelled.");
        }
    }

    private static void updateStaffPass() throws RHException {
        Staff s = findStaffByUsername();
        if (s == null) return;
        System.out.print("New password: ");
        s.setPassword(sc.nextLine().trim());
        System.out.println("Password updated.");
    }

    private static Staff findStaffByUsername() {
        System.out.print("Staff username: ");
        String u = sc.nextLine().trim();
        Staff s = home.findStaffByUsername(u);
        if (s == null) System.out.println("Staff not found.");
        return s;
    }

    private static void assignNurseShift() throws RHException {
        Nurse n = (Nurse)findStaffByUsername();
        if (n == null) return;

        System.out.print("DayOfWeek (e.g., MONDAY): ");
        DayOfWeek day = safeDayOfWeek(sc.nextLine().trim());
        if (day == null) { System.out.println("Invalid day."); return; }

        System.out.print("Shift: (1) 08:00-16:00 or (2) 14:00-22:00 ? ");
        String s = sc.nextLine().trim();
        LocalTime start = s.equals("2") ? LocalTime.of(14,0) : LocalTime.of(8,0);
        LocalTime end   = s.equals("2") ? LocalTime.of(22,0) : LocalTime.of(16,0);

        home.assignNurseShift(n, new Shift(day, start, end));
        System.out.println("Assigned: " + n.getName() + " -> " + day + " " + start + "-" + end);
    }

    private static void assignDoctorHour() throws RHException {
        Doctor d = (Doctor)findStaffByUsername();
        if (d == null) return;

        System.out.print("DayOfWeek (e.g., MONDAY): ");
        DayOfWeek day = safeDayOfWeek(sc.nextLine().trim());
        if (day == null) { System.out.println("Invalid day."); return; }

        System.out.print("Start hour (0-23) for 1-hour slot: ");
        Integer h = safeInt(sc.nextLine().trim());
        if (h == null || h < 0 || h > 23) { System.out.println("Invalid hour."); return; }

        home.assignDoctorHour(d, new Shift(day, LocalTime.of(h,0), LocalTime.of(h+1,0)));
        System.out.println("Assigned doctor hour.");
    }

    // Residents and beds related operations
    private static void addResidentToBed() throws RHException {
        System.out.print("Resident name: ");
        String name = sc.nextLine().trim();
        System.out.print("Gender (M/F): ");
        Gender g = sc.nextLine().trim().toUpperCase().startsWith("M") ? Gender.MALE : Gender.FEMALE;
        Resident r = new Resident(UUID.randomUUID().toString(), name, g);

        int[] wrb = promptWRB("Target location (Ward Room Bed) as 1-based, e.g., 1 3 2: ");
        if (wrb == null) return;

        home.allocateResident(r, wrb[0], wrb[1], wrb[2], getManagerActor());
        System.out.println("Resident allocated: " + r);
    }

    private static void moveResident() throws RHException {
        int[] from = promptWRB("From (Ward Room Bed) 1-based: ");
        if (from == null) return;
        int[] to = promptWRB("To (Ward Room Bed) 1-based: ");
        if (to == null) return;

        home.moveResident(from[0], from[1], from[2], to[0], to[1], to[2], getManagerActor());
        System.out.println("Successfully Moved.");
    }

    private static void viewBed() throws RHException {
        int[] loc = promptWRB("Enter Ward Room Bed (e.g., 1 2 1): ");
        if (loc == null) return;

        Bed b = home.getBed(loc[0], loc[1], loc[2]);
        System.out.println("\n" + b.describe());
    }

    private static void runCompliance() {
        try {
            home.checkCompliance();
            System.out.println("Compliance OK.");
        } catch (RHException e) {
            System.out.println("Compliance FAIL: " + e.getMessage());
        }
    }

    private static void dischargeResident() throws RHException, IOException {
        int[] loc = promptWRB("Ward Room Bed (resident to discharge) 1-based: ");
        if (loc == null) return;

        Bed b = home.getBed(loc[0], loc[1], loc[2]);
        if (b.getResident() == null) throw new ValidationException("No resident in that bed.");
        home.dischargeResident(b.getResident(), getManagerActor());
        System.out.println("Resident discharged and archived.");
    }

    // Operations that can be performed only by doctors and nurses.
    private static void attachPrescriptionWithDoctorLogin() throws RHException {
        Doctor doctor = promptDoctorLogin();
        if (doctor == null) { System.out.println("Doctor authentication failed."); return; }

        int[] loc = promptWRB("Ward Room Bed (resident) 1-based: ");
        if (loc == null) return;

        Bed b = home.getBed(loc[0], loc[1], loc[2]);
        if (b.getResident() == null) throw new ValidationException("No resident in that bed.");
        Resident r = b.getResident();

        System.out.print("Medication name: ");
        String med = sc.nextLine().trim();
        System.out.print("Dose (e.g., 500mg): ");
        String dose = sc.nextLine().trim();
        System.out.print("Times per day (comma of HH:mm, e.g., 08:00,20:00): ");
        String[] times = sc.nextLine().trim().split(",");
        List<LocalTime> at = new ArrayList<>();
        try {
            for (String t : times) at.add(LocalTime.parse(t.trim()));
        } catch (Exception e) {
            System.out.println("Invalid time format. Use HH:mm like 08:00,20:00");
            return;
        }

        Prescription p = new Prescription(doctor, r);
        p.addOrder(new MedicationOrder(med, dose, at));
        home.attachPrescription(doctor, r, p);
        System.out.println("Prescription attached.");
    }

    private static void recordAdministrationWithNurseLogin() throws RHException {
        Nurse nurse = promptNurseLogin();
        if (nurse == null) { System.out.println("Nurse authentication failed."); return; }

        int[] loc = promptWRB("Ward Room Bed (resident) 1-based: ");
        if (loc == null) return;

        Bed b = home.getBed(loc[0], loc[1], loc[2]);
        Resident r = b.getResident();
        if (r == null) throw new ValidationException("No resident in that bed.");

        System.out.print("Medication name: ");
        String med = sc.nextLine().trim();
        System.out.print("Dose (as prescribed string, e.g., 500mg): ");
        String dose = sc.nextLine().trim();

        // CareHome will also enforce roster requirement
        home.recordAdministration(nurse, r, med, dose, LocalDateTime.now());
        System.out.println("Administration recorded.");
    }

    // Authentication for doctor and nurse logins
    private static Doctor promptDoctorLogin() {
        System.out.print("Doctor username: ");
        String u = sc.nextLine().trim();
        System.out.print("Doctor password: ");
        String p = sc.nextLine().trim();
        Staff s = home.authenticate(u, p);
        return (s instanceof Doctor) ? (Doctor) s : null;
    }

    private static Nurse promptNurseLogin() {
        System.out.print("Nurse username: ");
        String u = sc.nextLine().trim();
        System.out.print("Nurse password: ");
        String p = sc.nextLine().trim();
        Staff s = home.authenticate(u, p);
        return (s instanceof Nurse) ? (Nurse) s : null;
    }

    // Utilities
    private static Manager getManagerActor() {
        // Use default manager (index 0) for audit logging of manager-driven actions
        return home.getManagers().get(0);
    }

    private static int[] promptWRB(String prompt) {
        System.out.print(prompt);
        String line = sc.nextLine().trim();
        String[] parts = line.split("\\s+");
        if (parts.length != 3) {
            System.out.println("Please enter exactly three integers, e.g., 1 3 2");
            return null;
        }
        Integer w = safeInt(parts[0]);
        Integer r = safeInt(parts[1]);
        Integer b = safeInt(parts[2]);
        if (w == null || r == null || b == null || w <= 0 || r <= 0 || b <= 0) {
            System.out.println("Ward/Room/Bed must be positive integers starting from 1.");
            return null;
        }
        return new int[]{w, r, b};
    }

    private static Integer safeInt(String s) {
        try { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return null; }
    }

    private static DayOfWeek safeDayOfWeek(String s) {
        try { return DayOfWeek.valueOf(s.toUpperCase()); }
        catch (Exception e) { return null; }
    }
}
