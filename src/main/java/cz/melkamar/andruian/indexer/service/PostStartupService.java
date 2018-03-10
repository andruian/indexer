package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.MongoPlaceRepository;
import cz.melkamar.andruian.indexer.dao.SolrPlaceRepository;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.Property;
import cz.melkamar.andruian.indexer.model.place.SolrPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostStartupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostStartupService.class);

    private final IndexService indexService;
    private final IndexerConfiguration indexerConfiguration;
    private final SolrPlaceRepository solrRepository;
    private final MongoPlaceRepository mongoRepository;

    @Autowired
    public PostStartupService(IndexService indexService,
                              IndexerConfiguration indexerConfiguration,
                              SolrPlaceRepository solrRepository,
                              MongoPlaceRepository mongoRepository) {
        this.indexService = indexService;
        this.indexerConfiguration = indexerConfiguration;
        this.solrRepository = solrRepository;
        this.mongoRepository = mongoRepository;
    }


    public void postStartup() {
        if (indexerConfiguration.isOnStartReindex()) {
            LOGGER.info("Triggering on-startup reindexing");
            indexService.reindexAll(false);
        }

//        listStuff();
//        saveToMongo();
//        readFromMongo();
    }

    public void saveToMongo() {
        Place place = new Place(
                50, 14, System.currentTimeMillis() + "", "clsUri",
                "locObjUri", new Property[]{
            new Property("foo", 42),
            new Property("xxx", "bar"),
        });
        mongoRepository.save(place);
        System.out.println("Saved something to mongo");
    }
    
    public void readFromMongo(){
        Optional<Place> a = mongoRepository.findByUri("1520007854325a");
        System.out.println(a.map(Place::toString).orElse("nothing"));
    }

    public void listStuff() {
        List<SolrPlace> places = solrRepository.findByTypeAndLocationWithin(
                "mytype",
                new Point(50.052828, 14.439898),
                new Distance(500));

        for (SolrPlace place : places) {
            System.out.println(place);
        }
        System.out.println("\n\n\n");
    }
}
