package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.SolrPlaceRepository;
import cz.melkamar.andruian.indexer.model.SolrPlace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;

import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostStartupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostStartupService.class);

    private final IndexService indexService;
    private final IndexerConfiguration indexerConfiguration;
    private final SolrPlaceRepository solrRepository;

    @Autowired
    public PostStartupService(IndexService indexService,
                              IndexerConfiguration indexerConfiguration,
                              SolrPlaceRepository solrRepository) {
        this.indexService = indexService;
        this.indexerConfiguration = indexerConfiguration;
        this.solrRepository = solrRepository;
    }


    public void postStartup() {
        if (indexerConfiguration.isOnStartReindex()) {
            LOGGER.info("Triggering on-startup reindexing");
            indexService.reindexAll();
        }
        
        listStuff();
    }

    public void listStuff(){
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
