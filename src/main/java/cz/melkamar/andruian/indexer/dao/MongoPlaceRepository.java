package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.model.place.Place;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface MongoPlaceRepository extends MongoRepository<Place, String> {
    Optional<Place> findByUri(String uri);
    List<Place> findAllByClassUri(String classUri);
}
