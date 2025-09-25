import java.io.Serializable;
import java.time.LocalTime;
import java.util.*;

public class MedicationOrder implements Serializable {
    private final String name;
    private final String dose;
    private final List<LocalTime> times;

    public MedicationOrder(String name, String dose, List<LocalTime> times) {
        this.name = name; this.dose = dose; this.times = new ArrayList<>(times);
    }

    public String name() { return name; }
    public String dose() { return dose; }
    public List<LocalTime> times() { return Collections.unmodifiableList(times); }

    @Override public String toString() { return name + " " + dose + " @" + times; }
}
