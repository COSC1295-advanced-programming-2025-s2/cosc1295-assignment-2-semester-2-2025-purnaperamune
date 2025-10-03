import java.io.Serializable;
import java.util.*;

public class Ward implements Serializable {
    private final String name;
    private final List<Room> rooms = new ArrayList<>();

    private Ward(String name) {
        this.name = name;
    }

    public static Ward of(String name, int[] bedsPerRoom) {
        Ward w = new Ward(name);
        for (int i = 0; i < bedsPerRoom.length; i++) {
            w.rooms.add(Room.withBeds("Room-" + (i+1), bedsPerRoom[i]));
        }
        return w;
    }

    public Room getRoom(int idx) {
        if (idx < 0 || idx >= rooms.size()) throw new IndexOutOfBoundsException("Invalid room idx");
        return rooms.get(idx);
    }

    public List<Room> getRooms() {
        return rooms; 
    }

    @Override public String toString() {
        return name;
    }
}
