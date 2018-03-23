package cz.melkamar.andruian.indexer.model.place;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.core.mapping.Dynamic;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.Map;

//@SolrDocument(solrCoreName = "${db.solr.collection}")
@SolrDocument(solrCoreName = "andruian") // TODO why is the property reference not resolving?
public class Place {
    @Id
    @Field
    private String uri;

    @Field
    private String type;

    @JsonIgnore
    @Field
    private Point location;

    @Field
    private String locationObjectUri;
    @Field
    private String label;

    @JsonIgnore
    @Field(value = "srcddf_dynstr")
    private String sourceDatadef;

    @Dynamic
    @Field(value = "*_prop_dynstr")
    private Map<String, String> properties;

    public Place(String sourceDatadef, String uri,
                 String type,
                 Point location,
                 String locationObjectUri,
                 String label,
                 Map<String, String> properties) {
        this.sourceDatadef = sourceDatadef;
        this.uri = uri;
        this.type = type;
        this.location = location;
        this.locationObjectUri = locationObjectUri;
        this.label = label;
        this.properties = properties;
    }

    public double getLatPos(){
        return location.getX();
    }

    public double getLongPos(){
        return location.getY();
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

    public String getLocationObjectUri() {
        return locationObjectUri;
    }

    public void setLocationObjectUri(String locationObjectUri) {
        this.locationObjectUri = locationObjectUri;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        StringBuilder proptxt = new StringBuilder();
        for (Map.Entry<String, String> stringStringEntry : properties.entrySet()) {
            proptxt.append(stringStringEntry.getKey()).append(": ").append(stringStringEntry.getValue()).append("\n");
        }

        return "Place{" +
                "uri='" + uri + '\'' +
                ", type='" + type + '\'' +
                ", location=" + location +
                ", locationObjectUri='" + locationObjectUri + '\'' +
                ", label='" + label + '\'' +
                ", properties=" + proptxt.toString() +
                '}';
    }
}
