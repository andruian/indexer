package cz.melkamar.andruian.indexer.dao;

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
 * A Data Access Object for retrieving {@link Place} objects indexed in the system.
 */
@Component
public class PlaceDAO {
    private final static Logger LOGGER = LoggerFactory.getLogger(PlaceDAO.class);

    private final SolrPlaceRepository solrPlaceRepository;

    @Autowired
    public PlaceDAO(SolrPlaceRepository solrPlaceRepository) {
        this.solrPlaceRepository = solrPlaceRepository;
    }

    /**
     * Save and index a new place object in the system.
     * @param place The place to index.
     */
    public void savePlace(Place place) {
        LOGGER.debug("Indexing a place: " + place);

        solrPlaceRepository.save(place);
    }

    /**
     * Save and index a list of places in the system.
     * @param places A list of places to index.
     */
    public void savePlaces(List<Place> places) {
        LOGGER.debug("Indexing " + places.size() + " places");
        if (places.isEmpty()) return;

        solrPlaceRepository.saveAll(places);
    }

    /**
     * Delete all places that are described by a particular data definition.
     * @param dataDefIri The IRI of a andr:DataDef object for which to delete all matching places.
     */
    public void deletePlacesOfDataDefIri(String dataDefIri) {
        LOGGER.debug("Deleting all places from datadef " + dataDefIri);
        solrPlaceRepository.deleteAllBySourceDatadef(dataDefIri);
    }

    /**
     * Get all places indexed in the system.
     *
     * @return A list of {@link Place} representing the matching data.
     */
    public List<Place> getAllPlaces() {
        LOGGER.debug("Fetching all places");
        List<Place> result = new ArrayList<>();
        solrPlaceRepository.findAll().forEach(result::add);
        return result;
    }

    /**
     * Get all places of the given RDF class indexed in the system.
     *
     * @param classUri The IRI of a RDF class to search for.
     * @return A list of {@link Place} representing the matching data.
     */
    public List<Place> getPlacesOfClass(String classUri) {
        return solrPlaceRepository.findByType(classUri);
    }

    /**
     * Get places inside an area defined by the coordinates.
     *
     * @param latCoord  The latitude of the center of the area to search in.
     * @param longCoord The longitude of the center of the area to search in.
     * @param radius    The radius from the center of the area to search in, in kilometers.
     * @return A list of {@link Place} representing the matching data.
     */
    public List<Place> getPlacesAroundPoint(double latCoord, double longCoord, double radius) {
        LOGGER.debug("getPlacesAroundPoint {} {} {}", latCoord, longCoord, radius);
        List<Place> result = solrPlaceRepository.findByLocationWithin(new Point(latCoord, longCoord),
                new Distance(radius));
        LOGGER.debug("getPlacesAroundPoint - found {} Places", result.size());
        return result;
    }

    /**
     * Get places inside an area defined by the coordinates, of a particular RDF class.
     *
     * @param classUri  The IRI of a RDF class to search for.
     * @param latCoord  The latitude of the center of the area to search in.
     * @param longCoord The longitude of the center of the area to search in.
     * @param radius    The radius from the center of the area to search in, in kilometers.
     * @return A list of {@link Place} representing the matching data.
     */
    public List<Place> getPlacesAroundPointOfClass(String classUri, double latCoord, double longCoord, double radius) {
        LOGGER.debug("getPlacesAroundPointOfClass {} {} {} {}", classUri, latCoord, longCoord, radius);
        List<Place> result = solrPlaceRepository.findByTypeAndLocationWithin(classUri,
                new Point(latCoord, longCoord),
                new Distance(radius));
        LOGGER.debug("getPlacesAroundPointOfClass - found {} Places", result.size());

        return result;
    }

    public int getDatadefPlacesCount(String dataDefIri) {
        return solrPlaceRepository.countAllBySourceDatadef(dataDefIri);
    }
}
