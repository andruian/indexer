package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.SolrPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    
    private final MongoPlaceRepository mongoPlaceRepository;
    private final SolrPlaceRepository solrPlaceRepository;

    @Autowired
    public PlaceDAO(MongoPlaceRepository mongoPlaceRepository,
                    SolrPlaceRepository solrPlaceRepository) {
        this.mongoPlaceRepository = mongoPlaceRepository;
        this.solrPlaceRepository = solrPlaceRepository;
    }

    public void savePlace(Place place) {
        LOGGER.debug("Indexing a place: "+place);
        
        solrPlaceRepository.save(new SolrPlace(place));
        mongoPlaceRepository.save(place);
    }

    public void savePlaces(List<Place> places) {
        LOGGER.debug("Indexing "+places.size()+" places");
        if (places.isEmpty()) return;
        
        solrPlaceRepository.saveAll(places.stream().map(SolrPlace::new).collect(Collectors.toList()));
        mongoPlaceRepository.saveAll(places);
    }

    public void deletePlace(Place place) {
        throw new NotImplementedException();
    }

    public List<Place> getAllPlaces() {
        LOGGER.debug("Fetching all places");
        return mongoPlaceRepository.findAll();
    }

    public List<Place> getPlacesOfClass(String classUri) {
        return mongoPlaceRepository.findAllByClassUri(classUri);
    }

    public List<Place> getPlacesAroundPoint(double latCoord, double longCoord, double radius) {
        throw new NotImplementedException();
    }

    public List<Place> getPlacesAroundPointOfClass(String classUri, double latCoord, double longCoord, double radius) {
        List<SolrPlace> solrPlaces = solrPlaceRepository.findByTypeAndLocationWithin(classUri,
                                                                                     new Point(latCoord, longCoord),
                                                                                     new Distance(radius));
        List<Place> result = new ArrayList<>(solrPlaces.size());
        for (SolrPlace solrPlace: solrPlaces){
            mongoPlaceRepository.findByUri(solrPlace.getUri()).ifPresent(result::add);
        }
        
        return result;
    }
}
