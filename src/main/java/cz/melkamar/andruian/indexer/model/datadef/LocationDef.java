package cz.melkamar.andruian.indexer.model.datadef;

import java.util.HashMap;
import java.util.Map;

public class LocationDef extends ClassDef {
    private Map<String, LocationClassToPropPath> pathsToGps;

    public LocationDef(String sparqlEndpoint, String classUri) {
        super(sparqlEndpoint, classUri);
        this.pathsToGps = new HashMap<>();
    }

    public LocationClassToPropPath getPathToGps(String locationClassUri) {
        return pathsToGps.get(locationClassUri);
    }
}
