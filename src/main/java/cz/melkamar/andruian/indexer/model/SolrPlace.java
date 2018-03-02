package cz.melkamar.andruian.indexer.model;

import cz.melkamar.andruian.indexer.model.place.Place;
import org.apache.solr.client.solrj.beans.Field;

/**
 * A representation of {@link cz.melkamar.andruian.indexer.model.place.Place} for storing in Solr.
 * <p>
 * We will only be storing the URI, type (class) and location of places. This is minimal data necessary for
 * indexing. Additional data will be stored elsewhere.
 */
public class SolrPlace {
    private String uri;
    private String type;
    private double latitude;
    private double longitude;

    // No-arg constructor required for Solr binding
    public SolrPlace() {
    }

    public SolrPlace(String uri, String type, double latitude, double longitude) {
        this.uri = uri;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public SolrPlace(Place place) {
        this.uri = place.getUri();
        this.type = place.getClassUri();
        this.latitude = place.getLatPos();
        this.longitude = place.getLongPos();
    }

    public String getUri() {
        return uri;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getType() {
        return type;
    }

    public String getLocation() {
        return latitude + "," + longitude;
    }

    @Field
    public void setUri(String uri) {
        this.uri = uri;
    }

    @Field
    public void setLocation(String location) {
        String[] parts = location.split(",");
        latitude = Double.parseDouble(parts[0]);
        longitude = Double.parseDouble(parts[1]);
    }

    @Field
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SolrPlace{" +
                "uri='" + uri + '\'' +
                ", type='" + type + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
