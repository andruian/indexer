package cz.melkamar.andruian.indexer.model.datadef;

public class DataDef {
    private final String uri;
    private final LocationDef locationDef;
    private final DataClassDef dataClassDef;

    public DataDef(String uri,
                   LocationDef locationDef,
                   DataClassDef dataClassDef) {
        this.uri = uri;
        this.locationDef = locationDef;
        this.dataClassDef = dataClassDef;
    }

    public String getUri() {
        return uri;
    }

    public LocationDef getLocationDef() {
        return locationDef;
    }

    public DataClassDef getDataClassDef() {
        return dataClassDef;
    }

    @Override
    public String toString() {
        return "DataDef{" +
                "uri='" + uri + '\'' +
                ", locationDef=" + locationDef +
                ", dataClassDef=" + dataClassDef +
                '}';
    }
}
