package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.exception.QueryFormatException;
import cz.melkamar.andruian.indexer.model.place.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QueryService {
    private final static Logger LOGGER = LoggerFactory.getLogger(QueryService.class);

    private final PlaceDAO placeDAO;

    @Autowired
    public QueryService(PlaceDAO placeDAO) {
        this.placeDAO = placeDAO;
    }

    public List<Place> query(@Nullable String type, @Nullable Double latitude, @Nullable Double longitude, @Nullable Double radius)
            throws QueryFormatException {
        LOGGER.debug("Executing query: type {} | pos {} {} | rad {}", type, latitude, longitude, radius);

        List<Place> data;
        if (type == null || type.isEmpty()){
            if (checkLatLongR(latitude, longitude, radius)) data = placeDAO.getPlacesAroundPoint(latitude, longitude, radius);
            else data = placeDAO.getAllPlaces();
        } else { // type not null
            if (checkLatLongR(latitude, longitude, radius)) data = placeDAO.getPlacesAroundPointOfClass(type, latitude, longitude, radius);
            else data = placeDAO.getPlacesOfClass(type);
        }

        LOGGER.trace("Query complete. Returning "+data.size());
        return data;
    }

    public List<Place> clusteredQuery(@Nullable String type, @Nullable Double latitude, @Nullable Double longitude, @Nullable Double radius)
            throws QueryFormatException {
        LOGGER.debug("Executing clustered query: type {} | pos {} {} | rad {}", type, latitude, longitude, radius);
        throw new NotImplementedException();
    }

    private boolean checkLatLongR(Double latitude, Double longitude, Double radius) throws QueryFormatException {
        if (latitude==null &&longitude==null && radius == null) return false;
        if (latitude!=null &&longitude!=null && radius != null) return true;
        throw new QueryFormatException("All of 'lat', 'long' and 'r' must be either provided or not provided.");
    }
}
