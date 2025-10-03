import java.io.Serializable;
import java.time.*;

public record Shift(DayOfWeek day, LocalTime start, LocalTime end) implements Serializable { }
