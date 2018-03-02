package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.model.SolrPlace;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.solr.repository.SolrCrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceSolrRepository extends SolrCrudRepository<SolrPlace, String> {
    List<SolrPlace> findByTypeAndLocationWithin(String type, Point location, Distance distance);
}
