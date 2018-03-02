package cz.melkamar.andruian.indexer.model;

import cz.melkamar.andruian.indexer.model.place.Place;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.core.mapping.SolrDocument;

/**
 * A representation of {@link cz.melkamar.andruian.indexer.model.place.Place} for storing in Solr.
 * <p>
 * We will only be storing the URI, type (class) and location of places. This is minimal data necessary for
 * indexing. Additional data will be stored elsewhere.
 */
@SolrDocument(solrCoreName = "${db.solr.collection}")
public class SolrPlace {
    @Id
    @Field
    private String uri;
    
    @Field
    private String type;

    @Field
    private Point location;
    
    // No-arg constructor required for Solr binding
    public SolrPlace() {
    }

    public SolrPlace(String uri, String type, double latitude, double longitude) {
        this.uri = uri;
        this.type = type;
        this.location = new Point(latitude, longitude);
    }

    public SolrPlace(Place place) {
        this.uri = place.getUri();
        this.type = place.getClassUri();
        this.location = new Point(place.getLatPos(), place.getLongPos());
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "SolrPlace{" +
                "uri='" + uri + '\'' +
                ", type='" + type + '\'' +
                ", location=" + location +
                '}';
    }
}
