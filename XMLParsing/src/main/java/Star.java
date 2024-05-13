public class Star {

    private final String id;

    private final String name;

    private final int birthyear;

    public Star(String id, String name, int birthyear) {
        this.id = id;
        this.name = name;
        this.birthyear = birthyear;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBirthYear() {
        return birthyear;
    }

    public String toString() {
        return getId() + "," + getName() + "," + getBirthYear();
    }
}
