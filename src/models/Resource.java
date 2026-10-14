package models;

/** A teaching resource such as a lab, hall or classroom (immutable). */
public class Resource {

    private final String id;
    private final String name;
    private final String type;
    private final int capacity;

    public Resource(String id, String name, String type, int capacity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + type + " | capacity=" + capacity;
    }
}
