package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.IndexerApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IndexCron {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexerApplication.class);

    @Scheduled(fixedDelay = 5000)
    public void triggerReindex(){
        LOGGER.warn("Reindexing...");
    }

}
