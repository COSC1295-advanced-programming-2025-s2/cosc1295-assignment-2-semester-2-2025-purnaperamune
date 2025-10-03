import java.io.Serializable;

// Generic assignment wrapper (staff + shift)
public record ShiftAssignment<T extends Staff>(T staff, Shift shift) implements Serializable { }
