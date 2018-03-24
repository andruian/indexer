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
    private String iri;

    @Field
    private String type;

    @JsonIgnore
    @Field
    private Point location;

    @Field
    private String locationObjectIri;
    @Field
    private String label;

    @JsonIgnore
    @Field(value = "srcddf_dynstr")
    private String sourceDatadef;

    @Dynamic
    @Field(value = "*_prop_dynstr")
    private Map<String, String> properties;

    public Place(String sourceDatadef, String iri,
                 String type,
                 Point location,
                 String locationObjectIri,
                 String label,
                 Map<String, String> properties) {
        this.sourceDatadef = sourceDatadef;
        this.iri = iri;
        this.type = type;
        this.location = location;
        this.locationObjectIri = locationObjectIri;
        this.label = label;
        this.properties = properties;
    }

    public double getLatPos(){
        return location.getX();
    }

    public double getLongPos(){
        return location.getY();
    }

    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
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

    public String getLocationObjectIri() {
        return locationObjectIri;
    }

    public void setLocationObjectIri(String locationObjectIri) {
        this.locationObjectIri = locationObjectIri;
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
                "iri='" + iri + '\'' +
                ", type='" + type + '\'' +
                ", location=" + location +
                ", locationObjectUri='" + locationObjectIri + '\'' +
                ", label='" + label + '\'' +
                ", properties=" + proptxt.toString() +
                '}';
    }
}
