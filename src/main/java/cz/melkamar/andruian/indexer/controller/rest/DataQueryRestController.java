package cz.melkamar.andruian.indexer.controller.rest;

import cz.melkamar.andruian.indexer.exception.QueryFormatException;
import cz.melkamar.andruian.indexer.model.place.PlaceCluster;
import cz.melkamar.andruian.indexer.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/query")
public class DataQueryRestController {
    private final QueryService queryService;

    @Autowired
    public DataQueryRestController(QueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * An endpoint method for querying data. The data is returned as JSON.
     *
     * The returned data may be of different types. The type is specified by an ID under responseType key. The actual
     * data is provided under the responseBody key.
     *
     * By default a list of places is returned, e.g
     * <pre>
     * {
     *   "responseType": 1,
     *   "responseBody": [
     *     {
     *       "iri": "http://src.com/consonant/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F22655883",
     *       "type": "http://example.org/SourceObjectConsonant",
     *       "locationObjectIri": "https://ruian.linked.opendata.cz/zdroj/adresní-místa/22655883",
     *       "label": "Holandská 18",
     *       "properties": {
     *         "StreetName": "Holandská",
     *         "PSC": "10100",
     *         "StreetNum": "18"
     *       },
     *       "latPos": 50.069716,
     *       "longPos": 14.454531
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * If clustering is enabled and activated, the response body will contain a list of clusters - the position
     * and number of places belonging to each cluster, like this:
     * <pre>
     * {
     *   "responseType": 2,
     *   "responseBody": [
     *     {
     *       "placesCount": 1,
     *       "latPos": 50.07072687149048,
     *       "longPos": 14.455454349517822
     *     },
     *     ...
     *   ]
     * }
     * </pre>
     *
     * @param type         The IRI of the RDF class to search for. If not provided, objects of all classes are returned.
     * @param latitude     The latitude around which to search for objects. Specified as floating-point coordinate
     *                     using the WGS84 system. Either none or all of latitude, longitude and radius must be provided.
     * @param longitude    The longitude around which to search for objects. Specified as floating-point coordinate
     *                     using the WGS84 system. Either none or all of latitude, longitude and radius must be provided.
     * @param radius       The radius of the circle, centered on the given lat long coordinates, in which to search
     *                     for objects. The unit is kilometers. Either none or all of latitude, longitude and radius must be provided.
     * @param showCount    If true, only return the number of matching objects, without the actual object data.
     * @param clusterLimit The maximum number of places to return without clustering. If provided and the number of
     *                     places to be returned is higher than this number, clusters will be returned instead.
     * @return A JSON-serialized {@link QueryResponse} object describing the matching data.
     */
    @RequestMapping
    public Object query(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "lat", required = false) Double latitude,
            @RequestParam(value = "long", required = false) Double longitude,
            @RequestParam(value = "r", required = false) Double radius,
            @RequestParam(value = "count", required = false) boolean showCount,
            @RequestParam(value = "clusterLimit", required = false) Integer clusterLimit
    ) {
        try {
            if (!showCount && (clusterLimit == null || clusterLimit == 0)) {
                return new QueryResponse(QueryResponse.RESPONSE_ALL, queryService.query(type, latitude, longitude, radius));
            }

            // Calculate places count using clustered query - very quick
            List<PlaceCluster> clusters = queryService.clusteredQuery(type, latitude, longitude, radius);
            long placesCount = clusters.stream().mapToLong(PlaceCluster::getPlacesCount).sum();

            if (showCount) return new QueryResponse(QueryResponse.RESPONSE_COUNT, placesCount);

            // No clustering limit set or number of places lower than the limit
            if (clusterLimit == null || placesCount <= clusterLimit) {
                return new QueryResponse(QueryResponse.RESPONSE_ALL, queryService.query(type, latitude, longitude, radius));
            } else {
                return new QueryResponse(QueryResponse.RESPONSE_CLUSTERED, clusters);
            }

        } catch (QueryFormatException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.toString());
        }
    }

    /**
     * A wrapper for the result of a query.
     */
    static class QueryResponse {
        /**
         * The response contains only the number of matching places.
         */
        public static final int RESPONSE_COUNT = 0;
        /**
         * The response contains places.
         */
        public static final int RESPONSE_ALL = 1;
        /**
         * The response contains clusters of places.
         */
        public static final int RESPONSE_CLUSTERED = 2;

        private final int responseType;
        private final Object responseBody;

        QueryResponse(int responseType, Object responseBody) {
            this.responseType = responseType;
            this.responseBody = responseBody;
        }

        /**
         * The type of the response.
         */
        public int getResponseType() {
            return responseType;
        }

        /**
         * The body of the response.
         */
        public Object getResponseBody() {
            return responseBody;
        }
    }
}
