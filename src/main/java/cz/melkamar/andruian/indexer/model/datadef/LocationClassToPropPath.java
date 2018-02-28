package cz.melkamar.andruian.indexer.model.datadef;

public class LocationClassToPropPath {
    private PropertyPath latCoord;
    private PropertyPath longCoord;

    public LocationClassToPropPath(PropertyPath latCoord, PropertyPath longCoord) {
        this.latCoord = latCoord;
        this.longCoord = longCoord;
    }

    public PropertyPath getLatCoord() {
        return latCoord;
    }

    public PropertyPath getLongCoord() {
        return longCoord;
    }
}
