package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.dao.ClusteredPlaceDAO;
import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.exception.QueryFormatException;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.PlaceCluster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * A service providing querying functionality. It communicates with the Data Access Objects to fetch
 * data based on given parameters.
 */
@SuppressWarnings("Duplicates")
@Service
public class QueryService {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

    private final PlaceDAO placeDAO;
    private final ClusteredPlaceDAO clusteredPlaceDAO;

    @Autowired
    public QueryService(PlaceDAO placeDAO, ClusteredPlaceDAO clusteredPlaceDAO) {
        this.placeDAO = placeDAO;
        this.clusteredPlaceDAO = clusteredPlaceDAO;
    }

    /**
     * Run a query and return matching data.
     *
     * @param type      The IRI of the RDF class to search for. If not provided, objects of all classes are returned.
     * @param latitude  The latitude around which to search for objects. Specified as floating-point coordinate
     *                  using the WGS84 system. Either none or all of latitude, longitude and radius must be provided.
     * @param longitude The longitude around which to search for objects. Specified as floating-point coordinate
     *                  using the WGS84 system. Either none or all of latitude, longitude and radius must be provided.
     * @param radius    The radius of the circle, centered on the given lat long coordinates, in which to search
     *                  for objects. The unit is kilometers. Either none or all of latitude, longitude and radius must be provided.
     * @return A list of places that match the given query.
     * @throws QueryFormatException The format of the query is invalid - likely because some but not all of latitude, longitude and radius were provided.
     */
    public List<Place> query(@Nullable String type,
                             @Nullable Double latitude,
                             @Nullable Double longitude,
                             @Nullable Double radius)
            throws QueryFormatException {
        LOGGER.debug("Executing query: type {} | pos {} {} | rad {}", type, latitude, longitude, radius);

        List<Place> data;
        if (type == null || type.isEmpty()) {
            if (checkLatLongR(latitude, longitude, radius))
                data = placeDAO.getPlacesAroundPoint(latitude, longitude, radius);
            else data = placeDAO.getAllPlaces();
        } else { // type not null
            if (checkLatLongR(latitude, longitude, radius))
                data = placeDAO.getPlacesAroundPointOfClass(type, latitude, longitude, radius);
            else data = placeDAO.getPlacesOfClass(type);
        }

        LOGGER.trace("Query complete. Returning " + data.size());
        return data;
    }

    /**
     * Run a query and return clusters of matching data.
     *
     * @param type      The IRI of the RDF class to search for. If not provided, objects of all classes are returned.
     * @param latitude  The latitude around which to search for objects. Specified as floating-point coordinate
     *                  using the WGS84 system. Either none or all of latitude, longitude and radius must be provided.
     * @param longitude The longitude around which to search for objects. Specified as floating-point coordinate
     *                  using the WGS84 system. Either none or all of latitude, longitude and radius must be provided.
     * @param radius    The radius of the circle, centered on the given lat long coordinates, in which to search
     *                  for objects. The unit is kilometers. Either none or all of latitude, longitude and radius must be provided.
     * @return A list of clusters containing places that match the given query.
     * @throws QueryFormatException The format of the query is invalid - likely because some but not all of latitude, longitude and radius were provided.
     */
    public List<PlaceCluster> clusteredQuery(@Nullable String type,
                                             @Nullable Double latitude,
                                             @Nullable Double longitude,
                                             @Nullable Double radius)
            throws QueryFormatException {
        LOGGER.debug("Executing clustered query: type {} | pos {} {} | rad {}", type, latitude, longitude, radius);

        List<PlaceCluster> data;
        if (type == null || type.isEmpty()) {
            if (checkLatLongR(latitude, longitude, radius))
                data = clusteredPlaceDAO.getPlacesAroundPoint(latitude, longitude, radius);
            else data = clusteredPlaceDAO.getAllPlaces();
        } else { // type not null
            if (checkLatLongR(latitude, longitude, radius))
                data = clusteredPlaceDAO.getPlacesAroundPointOfClass(type, latitude, longitude, radius);
            else data = clusteredPlaceDAO.getPlacesOfClass(type);
        }

        LOGGER.trace("Query complete. Returning " + data.size());
        return data;
    }

    /**
     * Checks that either none or all of latitude, longitude and radius are provided.
     * @return True if none or all parametres are non-null.
     * @throws QueryFormatException When one or more, but not all parameters are provided.
     */
    private boolean checkLatLongR(Double latitude, Double longitude, Double radius) throws QueryFormatException {
        if (latitude == null && longitude == null && radius == null) return false;
        if (latitude != null && longitude != null && radius != null) return true;
        throw new QueryFormatException("All of 'lat', 'long' and 'r' must be either provided or not provided.");
    }
}
