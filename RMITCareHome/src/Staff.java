import java.io.Serializable;

public abstract class Staff implements Serializable {
    private final String id;
    private final String name;
    private final String username;
    private String password;

    protected Staff(String id, String name, String username) {
        this.id = id; this.name = name; this.username = username;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean checkPassword(String pw) {
        return password != null && password.equals(pw);
    }

    @Override public String toString() {
        return getClass().getSimpleName() + "{" + name + ", user=" + username + "}";
    }
}
