package cz.melkamar.andruian.indexer.model.place;

public class Property<T> {
    private final String name;
    private final T value;

    public Property(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }
}
