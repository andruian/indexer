package cz.melkamar.andruian.indexer.net;


import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.Property;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SparqlConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlConnector.class);

    public static void main(String[] args) {
        String query = "PREFIX skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                "PREFIX eu: <http://eulersharp.sourceforge.net/2003/03swap/log-rules#>\n" +
                "PREFIX ru: <http://purl.org/imbi/ru-meta.owl#>\n" +
                "prefix ex: <http://example.org/>\n" +
                "prefix ruian: <http://ruian.linked.opendata.cz/ontology/>\n" +
                "prefix s: <http://schema.org/>\n" +
                "prefix xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "\n" +
                "SELECT distinct ?dataObj ?locationObj ?lat ?long ?id\n" +
                "WHERE {\n" +
                "  ?dataObj a ex:MyObject;\n" +
                "         ex:someLink/ex:linksTo ?locationObj;\n" +
                "         .\n" +
                "\n" +
                "  #\n" +
                "  # Optional filter when reindexingto exclude all objects that already exist\n" +
                "  # Example contents of excludeDataObjects:\n" +
                "  #    ?dataObj != <http://example.org/linkedobject-24481611> &&\n" +
                "  #    ?dataObj != <http://example.org/linkedobject-72715057> &&\n" +
                "  #\n" +
                "  # This will filter out the two objects listed.\n" +
                "  # - Note that each line/expression MUST end with the && operator, including the last one,\n" +
                "  #   because there is a trailing True expression in the query template.\n" +
                "  #   The reason for that is to avoid parsing error thrown by FILTER() - there must be something in the parentheses.\n" +
                "  #\n" +
                "  FILTER(\n" +
                "    True\n" +
                "  )\n" +
                "\n" +
                "  #\n" +
                "  # Mapping of selectProps - name of any selectProp must NOT be any of the reserved ones (dataObj, locationObj etc.)\n" +
                "  # Example mapping:\n" +
                "  #   ?dataObj ex:a/ex:b/ex:c ?selectPropA .\n" +
                "  #\n" +
                "  # There will be one line per each selectProp\n" +
                "  ?dataObj ex:id ?id.\n" +
                "\n" +
                "  #\n" +
                "  # Federated query for the location sparql controller.\n" +
                "  #   [lat,long]LocationPathForLocationClass will contain a\n" +
                "  #   property path from the Location class to its coordinates.\n" +
                "  SERVICE <http://ruian.linked.opendata.cz/sparql> {\n" +
                "    ?locationObj ruian:adresniBod/s:geo/s:latitude ?lat;\n" +
                "                 ruian:adresniBod/s:geo/s:longitude ?long;\n" +
                "    .\n" +
                "\n" +
                "    #\n" +
                "    # RUIAN-specific\n" +
                "    #   For some reason RUIAN has two values for object's coordinates.\n" +
                "    #   Make sure we get the one we expect by filtering.\n" +
                "    FILTER(\n" +
                "      datatype(?lat) = xsd:decimal &&\n" +
                "      datatype(?long) = xsd:decimal\n" +
                "    )\n" +
                "  }\n" +
                "}";

        List<Place> places = new SparqlConnector().executeIndexQuery(query,
                                                                     "http://localhost:3030/test/query",
                                                                     new String[]{"id"});
        for (Place place : places) {
            System.out.println(place);
        }
    }

    public List<Place> executeIndexQuery(String queryStr, String sparqlEndpoint, String[] selectProperties) {
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

        return new Place(latitude, longitude, dataObjUri, dataClassType, locationObjUri, properties);
    }

}
