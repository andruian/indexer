package cz.melkamar.andruian.indexer.model.datadef;

public class ClassToCoordPropPath {
    private final PropertyPath latCoord;
    private final PropertyPath longCoord;
    private final String forClassUri;

    public ClassToCoordPropPath(PropertyPath latCoord, PropertyPath longCoord, String forClassUri) {
        this.latCoord = latCoord;
        this.longCoord = longCoord;
        this.forClassUri = forClassUri;
    }

    public PropertyPath getLatCoord() {
        return latCoord;
    }

    public PropertyPath getLongCoord() {
        return longCoord;
    }

    public String getForClassUri() {
        return forClassUri;
    }
}
