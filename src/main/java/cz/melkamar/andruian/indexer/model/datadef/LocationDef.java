package cz.melkamar.andruian.indexer.model.datadef;

import java.util.Map;

public class LocationDef extends ClassDef {
    private final Map<String, ClassToCoordPropPath> pathsToGps;

    public LocationDef(String sparqlEndpoint,
                       String classUri,
                       Map<String, ClassToCoordPropPath> pathsToGps) {
        super(sparqlEndpoint, classUri);
        this.pathsToGps = pathsToGps;
    }

    public Map<String, ClassToCoordPropPath> getPathsToGps() {
        return pathsToGps;
    }

    public ClassToCoordPropPath getPathToGps(String locationClassUri) {
        return pathsToGps.get(locationClassUri);
    }
}
