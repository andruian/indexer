package cz.melkamar.andruian.indexer.model.datadef;

public class PropertyPath {
    private String[] path;

    public PropertyPath(String[] path) {
        this.path = path;
    }

    public String pathToString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < path.length; i++) {
            builder.append("<").append(path[i]).append(">");
            if (i < path.length - 1) builder.append("/");
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return pathToString();
    }
}
