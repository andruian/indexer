package cz.melkamar.andruian.indexer.model.place;

public class Property<T> {
    private final String name;
    private final Object value;

    public Property(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "Property{" + name + "=" + value + "}";
    }
}
