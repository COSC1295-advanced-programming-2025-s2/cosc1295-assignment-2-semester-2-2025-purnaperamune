import java.io.Serializable;

public enum ActionType implements Serializable {
    ADD_RESIDENT,
    MOVE_RESIDENT,
    ADD_PRESCRIPTION,
    ADMINISTER_MED,
    DISCHARGE
}
