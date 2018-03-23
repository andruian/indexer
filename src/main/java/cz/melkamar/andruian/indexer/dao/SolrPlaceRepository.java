package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.model.place.Place;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SolrPlaceRepository extends SolrCrudRepository<Place, String> {
    List<Place> findByTypeAndLocationWithin(String type, Point location, Distance distance);
    List<Place> findByLocationWithin(Point location, Distance distance);
    List<Place> findByType(String type);
    void deleteAllBySourceDatadef(String sourceDatadef);
    int countAllBySourceDatadef(String sourceDatadef);
}
