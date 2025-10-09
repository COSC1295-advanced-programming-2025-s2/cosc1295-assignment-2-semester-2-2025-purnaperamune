import java.io.IOException;
import java.time.*;
import java.util.*;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static CareHome home;

    public static void main(String[] args) {
        System.out.println("=== Care Home :: Resident HealthCare System (Phase 1, Console) ===");
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

    