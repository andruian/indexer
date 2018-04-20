package cz.melkamar.andruian.indexer.model.place;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.core.mapping.Dynamic;
import org.springframework.data.solr.core.mapping.SolrDocument;

import java.util.Map;

/**
 * A single place. When indexing, this object corresponds to one RDF object.
 */
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

    /**
     * The latitude of this object.
     */
    public double getLatPos(){
        return location.getX();
    }

    /**
     * The longitude of this object.
     */
    public double getLongPos(){
        return location.getY();
    }

    /**
     * The IRI of the RDF object this class was parsed from.
     */
    public String getIri() {
        return iri;
    }

    public void setIri(String iri) {
        this.iri = iri;
    }

    /**
     * The type (RDF class) of the object.
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * The location of the object as lat lng.
     */
    public Point getLocation() {
        return location;
    }

    public void setLocation(Point location) {
        this.location = location;
    }
    /**
     * The IRI of the associated location object to this object.
     */
    public String getLocationObjectIri() {
        return locationObjectIri;
    }

    public void setLocationObjectIri(String locationObjectIri) {
        this.locationObjectIri = locationObjectIri;
    }

    /**
     * A human-readable label for this object. If no label could be determined,
     * the IRI of the object is used instead.
     * @return
     */
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
