package cz.melkamar.andruian.indexer.model.datadef;

import java.util.Map;

public class LocationDef extends ClassDef {
    private final Map<String, LocationClassToPropPath> pathsToGps;

    public LocationDef(String sparqlEndpoint,
                       String classUri,
                       Map<String, LocationClassToPropPath> pathsToGps) {
        super(sparqlEndpoint, classUri);
        this.pathsToGps = pathsToGps;
    }

    public Map<String, LocationClassToPropPath> getPathsToGps() {
        return pathsToGps;
    }

    public LocationClassToPropPath getPathToGps(String locationClassUri) {
        return pathsToGps.get(locationClassUri);
    }
}
