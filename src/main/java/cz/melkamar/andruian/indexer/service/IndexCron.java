package cz.melkamar.andruian.indexer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class IndexCron {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Scheduled(fixedDelay = 5000)
    public void triggerReindex(){
        LOGGER.warn("Reindexing...");
    }

}
