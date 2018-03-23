package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.ddfparser.model.DataDef;
import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.model.place.Place;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Class orchestrating storing and retrieving {@link Place} objects.
 * <p>
 * They are stored in two separate databases:
 * - Solr for fast spatial queries (only location and uri)
 * - MongoDB for the whole objects, without indexing.
 * <p>
 * Methods of this class will ensure that both databases are used in unison.
 */

@Component
public class PlaceDAO {
    private final static Logger LOGGER = LoggerFactory.getLogger(PlaceDAO.class);

    private final SolrPlaceRepository solrPlaceRepository;

    @Autowired
    public PlaceDAO(SolrPlaceRepository solrPlaceRepository) {
        this.solrPlaceRepository = solrPlaceRepository;
    }

    public void savePlace(Place place) {
        LOGGER.debug("Indexing a place: " + place);

        solrPlaceRepository.save(place);
    }

    public void savePlaces(List<Place> places) {
        LOGGER.debug("Indexing " + places.size() + " places");
        if (places.isEmpty()) return;

        solrPlaceRepository.saveAll(places);
    }

    public void deletePlace(Place place) {
        throw new NotImplementedException();
    }

    public void deletePlacesOfDataDef(DataDef dataDef){
        LOGGER.debug("Deleting all places from datadef "+dataDef);
        solrPlaceRepository.deleteAllBySourceDatadef(dataDef.getUri());
    }

    public List<Place> getAllPlaces() {
        LOGGER.debug("Fetching all places");
        List<Place> result = new ArrayList<>();
        solrPlaceRepository.findAll().forEach(result::add);
        return result;
    }

    public List<Place> getPlacesOfClass(String classUri) {
        return solrPlaceRepository.findByType(classUri);
    }

    public List<Place> getPlacesAroundPoint(double latCoord, double longCoord, double radius) {
        LOGGER.debug("getPlacesAroundPoint {} {} {}", latCoord, longCoord, radius);
        List<Place> result = solrPlaceRepository.findByLocationWithin(new Point(latCoord, longCoord),
                                                                              new Distance(radius));
        LOGGER.debug("getPlacesAroundPoint - found {} Places", result.size());
        return result;
    }

    public List<Place> getPlacesAroundPointOfClass(String classUri, double latCoord, double longCoord, double radius) {
        LOGGER.debug("getPlacesAroundPointOfClass {} {} {} {}", classUri, latCoord, longCoord, radius);
        List<Place> result = solrPlaceRepository.findByTypeAndLocationWithin(classUri,
                                                                                     new Point(latCoord, longCoord),
                                                                                     new Distance(radius));
        LOGGER.debug("getPlacesAroundPointOfClass - found {} Places", result.size());

        return result;
    }

    public int getDatadefPlacesCount(DataDef dataDef){
        return solrPlaceRepository.countAllBySourceDatadef(dataDef.getUri());
    }
}
