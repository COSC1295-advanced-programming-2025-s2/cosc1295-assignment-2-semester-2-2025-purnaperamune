import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class AllocationAndMoveTest {

    CareHome home;
    Manager mgr;

    @BeforeEach
    void setup() {
        home = CareHome.defaultLayoutWithManager();
        mgr = home.getManagers().get(0);
    }

    @Test
    void allocateThenMoveResident_success() throws Exception {
        Resident r = new Resident("RID-1", "Alice", Gender.FEMALE);

        home.allocateResident(r, 1, 3, 1, mgr); // put Alice into Ward1/Room3/Bed1
        Bed from = home.getBed(1, 3, 1);
        assertEquals(r, from.getResident());

        // Move to another empty room/bed
        // pick Room3 bed2 (same room -> compatible gender)
        home.moveResident(1, 3, 1, 1, 3, 2, mgr);
        assertNull(from.getResident());
        Bed to = home.getBed(1, 3, 2);
        assertEquals(r, to.getResident());
    }

    @Test
    void cannotAllocateToOccupiedBed() throws Exception {
        Resident r1 = new Resident("RID-1", "Alice", Gender.FEMALE);
        Resident r2 = new Resident("RID-2", "Beth", Gender.FEMALE);

        home.allocateResident(r1, 1, 4, 1, mgr);
        assertThrows(BedOccupiedException.class, () -> home.allocateResident(r2, 1, 4, 1, mgr));
    }
}
