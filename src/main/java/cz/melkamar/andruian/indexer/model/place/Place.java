package cz.melkamar.andruian.indexer.model.place;

import org.apache.solr.client.solrj.beans.Field;

import java.util.Arrays;

public class Place {
    private final double latPos;
    private final double longPos;
    @Field
    private final String uri;
    private final String classUri;
    private final String locationObjectUri;
    private final Property[] properties;

    public Place(double latPos,
                 double longPos,
                 String uri,
                 String classUri,
                 String locationObjectUri,
                 Property[] properties) {
        this.latPos = latPos;
        this.longPos = longPos;
        this.uri = uri;
        this.classUri = classUri;
        this.locationObjectUri = locationObjectUri;
        this.properties = properties;
    }


    public double getLatPos() {
        return latPos;
    }

    public double getLongPos() {
        return longPos;
    }
    
    public String getUri() {
        return uri;
    }

    public String getClassUri() {
        return classUri;
    }

    public String getLocationObjectUri() {
        return locationObjectUri;
    }

    public Property[] getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        return "Place{" +
                "latPos=" + latPos +
                ", longPos=" + longPos +
                ", uri='" + uri + '\'' +
                ", classUri='" + classUri + '\'' +
                ", locationObjectUri='" + locationObjectUri + '\'' +
                ", properties=" + Arrays.toString(properties) +
                '}';
    }
}
