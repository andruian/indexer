package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.model.DataDefFile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MongoDataDefFileRepository extends MongoRepository<DataDefFile, String> {
}
