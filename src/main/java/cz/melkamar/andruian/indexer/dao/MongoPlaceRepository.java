package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.model.place.Place;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MongoPlaceRepository extends MongoRepository<Place, String> {
    List<Place> findAllByUriIn(Collection<String> uris);
    Optional<Place> findByUri(String uri);
    List<Place> findAllByClassUri(String classUri);
    List<Place> findAllByClassUriAndUriIn(String classUri, Collection<String> uris);
}
