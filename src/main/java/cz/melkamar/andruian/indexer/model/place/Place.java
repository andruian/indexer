package cz.melkamar.andruian.indexer.model.place;

import java.util.ArrayList;
import java.util.List;

public class Place {
    private double latPos;
    private double longPos;
    private String uri;
    private String classUri;
    private String locationObjectUri;
    private List<Property> properties;

    public Place(double latPos, double longPos, String uri, String classUri, String locationObjectUri) {
        this.latPos = latPos;
        this.longPos = longPos;
        this.uri = uri;
        this.classUri = classUri;
        this.locationObjectUri = locationObjectUri;
        this.properties = new ArrayList<>();
    }

    public void addProperty(Property property) {
        properties.add(property);
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

    public List<Property> getProperties() {
        return properties;
    }
}
