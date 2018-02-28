package cz.melkamar.andruian.indexer.rdf.sparql;

import cz.melkamar.andruian.indexer.Util;
import cz.melkamar.andruian.indexer.exception.ReservedNameUsedException;
import cz.melkamar.andruian.indexer.model.datadef.PropertyPath;
import cz.melkamar.andruian.indexer.model.datadef.SelectProperty;
import cz.melkamar.andruian.indexer.thirdparty.MapFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class IndexQueryBuilder extends SparqlQueryBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SparqlQueryBuilder.class);
    public static final String TEMPLATE_PATH = "templates/sparql/indexquery.sparql";
    public static final Set<String> RESERVED_VAR_NAMES = new HashSet<>(Arrays.asList("dataObj",
                                                                                     "locationObj",
                                                                                     "lat",
                                                                                     "long"));

    public IndexQueryBuilder(String dataClassUri,
                             PropertyPath dataToLocationClassPropPath,
                             String locationSparqlEndpoint,
                             PropertyPath locClassToLatPropPath,
                             PropertyPath locClassToLongPropPath) {
        this.dataClassUri = dataClassUri;
        this.dataToLocationClassPropPath = dataToLocationClassPropPath;
        this.locationSparqlEndpoint = locationSparqlEndpoint;
        this.locClassToLatPropPath = locClassToLatPropPath;
        this.locClassToLongPropPath = locClassToLongPropPath;
        this.selectProperties = new ArrayList<>();
        this.excludeDataObjUris = new ArrayList<>();
    }

    // Mandatory
    private final String dataClassUri;
    private final PropertyPath dataToLocationClassPropPath;
    private final String locationSparqlEndpoint;
    private final PropertyPath locClassToLatPropPath;
    private final PropertyPath locClassToLongPropPath;

    // Optional
    private List<SelectProperty> selectProperties;
    private List<String> excludeDataObjUris;

    public void addSelectProperty(SelectProperty selectProperty){
        selectProperties.add(selectProperty);
    }

    public void excludeUri(String excludeUri){
        excludeDataObjUris.add(excludeUri);
    }

    @Override
    public String build() {
        LOGGER.info("Building an index SPARQL query from template {}", TEMPLATE_PATH);
        String queryTemplate;
        try {
            queryTemplate = Util.readResourceFileToString(TEMPLATE_PATH);
        } catch (IOException e) {
            LOGGER.error("Could not find template query.", e);
            return null;
        }


        Map<String, String> argMap = new HashMap<>();
        argMap.put("dataClassUri", dataClassUri);
        argMap.put("pathToLocClass", dataToLocationClassPropPath.toString());
        argMap.put("locationSparqlEndpoint", locationSparqlEndpoint);
        argMap.put("latLocationPathForLocationClass", locClassToLongPropPath.toString());
        argMap.put("longLocationPathForLocationClass", locClassToLongPropPath.toString());
        argMap.put("selectProps", buildSelectProps(selectProperties));
        argMap.put("selectPropsMapping", buildSelectPropsMapping(selectProperties));
        argMap.put("excludeDataObjects", buildExcludeDataObjectsExpr(excludeDataObjUris));

        return MapFormat.format(queryTemplate, argMap);
    }

    /**
     * Build string of selected property names used in the SELECT section of the query.
     * Example, SelectProperty objects with names "a", "xyz", "propname" will be transformed into:
     * "?a ?xyz ?propname ".
     *
     * @param selectProperties List of SelectProperty objects which will be converted into a list of prop names.
     */
    private String buildSelectProps(List<SelectProperty> selectProperties) {
        StringBuilder builder = new StringBuilder();
        for (SelectProperty selectProperty : selectProperties) {
            checkReservedVariableUsed(selectProperty.getName());

            builder.append("?").append(selectProperty.getName()).append(" ");
        }
        return builder.toString();
    }

    /**
     * Build string of selected property mappings used in the body of the query.
     * <p>
     * Example, SelectProperty objects
     * <p>
     * (a | ex:a)
     * <p>
     * (xyz | ex:x/ex:y/ex:z)
     * <p>
     * Will be transformed into:
     * <p>
     * ?dataObj ex:a ?a .
     * <p>
     * ?dataObj ex:x/ex:y/ex:z ?xyz .
     *
     * @param selectProperties List of SelectProperty objects which will be converted into a mapping.
     */
    private String buildSelectPropsMapping(List<SelectProperty> selectProperties) {
        StringBuilder builder = new StringBuilder();
        for (SelectProperty selectProperty : selectProperties) {
            checkReservedVariableUsed(selectProperty.getName());

            builder.append("?dataObj ")
                    .append(selectProperty.getPath().toString())
                    .append(" ")
                    .append(selectProperty.getName())
                    .append(" .\n");
        }
        return builder.toString();
    }

    /**
     * Build filter expression for data objects that should be discarded from the query.
     * <p>
     * Example, to discard two objects, the following will be generated:
     * <pre>
     * {@code ?dataObj != <http://example.org/linkedobject-24481611> &&
     *   ?dataObj != <http://example.org/linkedobject-72715057> &&
     * }
     * </pre>
     *
     * @param excludeDataObjUris List of URIs as strings that will be filtered out of the query. E.g. "http://ex.org".
     *                           NOT &lt;http://ex.org&gt;.
     */
    private String buildExcludeDataObjectsExpr(List<String> excludeDataObjUris) {
        StringBuilder builder = new StringBuilder();
        for (SelectProperty selectProperty : selectProperties) {
            checkReservedVariableUsed(selectProperty.getName());

            builder.append("?dataObj != <").append(excludeDataObjUris).append("> &&\n");
        }
        return builder.toString();
    }

    private void checkReservedVariableUsed(String varName) {
        if (RESERVED_VAR_NAMES.contains(varName)) {
            throw new ReservedNameUsedException("This variable name is reserved and cannot be used: " + varName);
        }
    }
}
