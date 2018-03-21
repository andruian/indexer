package cz.melkamar.andruian.indexer.net;


import cz.melkamar.andruian.indexer.exception.SparqlQueryException;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.Property;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SparqlConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlConnector.class);

    public List<Place> executeIndexQuery(String queryStr, String sparqlEndpoint, String[] selectProperties)
            throws SparqlQueryException {
        Query query = QueryFactory.create(queryStr);
        QueryExecution qexec = QueryExecutionFactory
                .createServiceRequest(sparqlEndpoint, query);

        List<Place> resultList = new ArrayList<>();

        try {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                QuerySolution soln = results.nextSolution();
                resultList.add(placeFromQueryResult(soln, selectProperties));
            }
        } catch (Exception e) {
            throw new SparqlQueryException(e);
        } finally {
            qexec.close();
        }

        return resultList;
    }

    Place placeFromQueryResult(QuerySolution querySolution, String[] selectProperties) {
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

        Property[] properties = new Property[selectProperties.length];
        for (int i = 0; i < selectProperties.length; i++) {
            String propName = selectProperties[i];
            RDFNode propertyRdfNode = querySolution.get(propName);

            if (propertyRdfNode.isLiteral()) {
                properties[i] = new Property(propName, propertyRdfNode.asLiteral().getValue());
            } else { // Property is resource, just get its uri
                properties[i] = new Property(propName, propertyRdfNode.asResource().getURI());
            }
        }

        // Assign label to the place - prefer skor:prefLabel, then s:name, and fallback to empty string.
        String label = "";
        if (prefLabel != null) label = prefLabel;
        else if (name != null) label = name;

        return new Place(latitude, longitude, dataObjUri, dataClassType, locationObjUri, properties, label);
    }

}
