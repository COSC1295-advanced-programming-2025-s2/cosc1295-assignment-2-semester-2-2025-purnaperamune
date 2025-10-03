import java.io.Serializable;
import java.time.LocalDateTime;

public class ActionLog implements Serializable {
    private final LocalDateTime when;
    private final ActionType type;
    private final String staffId;
    private final String info;

    private ActionLog(LocalDateTime when, ActionType type, String staffId, String info) {
        this.when = when; this.type = type; this.staffId = staffId; this.info = info;
    }

    public static ActionLog now(ActionType t, String staffId, String info) {
        return new ActionLog(LocalDateTime.now(), t, staffId, info);
    }

    @Override public String toString() {
        return when + " [" + type + "] " + staffId + " :: " + info;
    }
}
