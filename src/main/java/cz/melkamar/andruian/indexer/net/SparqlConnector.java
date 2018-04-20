package cz.melkamar.andruian.indexer.net;


import cz.melkamar.andruian.indexer.exception.SparqlQueryException;
import cz.melkamar.andruian.indexer.model.place.Place;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A service facilitating SPARQL communication with endpoints.
 *
 * TODO change this class to use RDF4J, since the datadefparser library brings that dependency with itself anyway. No reason to use both Jena and RDF4J.
 */
@Service
public class SparqlConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlConnector.class);

    /**
     * Execute an indexing query and return the response data as a list of {@link Place} objects.
     * @param datadefIri The IRI of the data definition associated with the index query.
     * @param queryStr The full SPARQL query.
     * @param sparqlEndpoint The URL of the SPARQL endpoint where to send the query.
     * @param selectProperties A list of names of "select properties" which will be returned with the query
     *                         and should be saved with the returned objects.
     * @return A list of {@link Place} objects returned by the SPARQL endpoint.
     * @throws SparqlQueryException if, for any reason, the indexing failed.
     */
    public List<Place> executeIndexQuery(String datadefIri, String queryStr, String sparqlEndpoint, String[] selectProperties)
            throws SparqlQueryException {
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory
                .createServiceRequest(sparqlEndpoint, query);

        List<Place> resultList = new ArrayList<>();

        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                resultList.add(placeFromQueryResult(datadefIri, soln, selectProperties));
            }
        } catch (Exception e) {
            throw new SparqlQueryException(e);
        } finally {
            qexec.close();
        }

        return resultList;
    }

    /**
     * Construct a {@link Place} object from a single "line" of the indexing query result.
     * @param datadefIri The IRI of the data definition the place is described by.
     * @param querySolution The {@link QuerySolution} object describing the place in RDF.
     * @param selectProperties A list of extra properties to look for in the query result and save with the {@link Place} object.
     * @return A {@link Place} corresponding to the query result.
     */
    Place placeFromQueryResult(String datadefIri, QuerySolution querySolution, String[] selectProperties) {
        String dataObjUri = querySolution.getResource("dataObj").toString();
        String locationObjUri = querySolution.getResource("locationObj").toString();
        double latitude = querySolution.getLiteral("lat").getDouble();
        double longitude = querySolution.getLiteral("long").getDouble();
        String dataClassType = querySolution.getResource("dataClassType").toString();

        Literal prefLabelLit = querySolution.getLiteral("__prefLab__");
        Literal nameLit = querySolution.getLiteral("__name__");

        String prefLabel = null;
        if (prefLabelLit != null) prefLabel = querySolution.getLiteral("__prefLab__").getString();
        String name = null;
        if (nameLit != null) name = querySolution.getLiteral("__name__").getString();

        Map<String, String> properties = new HashMap<>();
        for (String propName : selectProperties) {
            RDFNode propertyRdfNode = querySolution.get(propName);

            if (propertyRdfNode != null) { // Properties are optional
                if (propertyRdfNode.isLiteral()) {
                    properties.put(propName, propertyRdfNode.asLiteral().getValue().toString());
                } else { // Property is resource, just get its uri
                    properties.put(propName, propertyRdfNode.asResource().getURI());
                }
            }
        }

        // Assign label to the place - prefer skor:prefLabel, then s:name, and fallback to empty string.
        String label = "";
        if (prefLabel != null) label = prefLabel;
        else if (name != null) label = name;

        return new Place(datadefIri,
                         dataObjUri, dataClassType, new Point(latitude, longitude), locationObjUri, label, properties);
    }

}
