public class Nurse extends Staff {
    private final Gender gender;

    public Nurse(String id, String name, String username, Gender gender) {
        super(id, name, username);
        this.gender = gender;
    }

    public Gender getGender() {
        return gender;
    }
}
