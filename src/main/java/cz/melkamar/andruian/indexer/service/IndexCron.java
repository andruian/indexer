package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@ConditionalOnProperty(IndexerConfiguration.INDEX_CRON)
public class IndexCron {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final IndexerConfiguration indexerConfiguration;
    private final IndexService indexService;

    @Autowired
    public IndexCron(IndexerConfiguration indexerConfiguration,
                     IndexService indexService) {
        this.indexerConfiguration = indexerConfiguration;
        this.indexService = indexService;
    }

    @Scheduled(cron = "${" + IndexerConfiguration.INDEX_CRON + "}")
    public void triggerReindex() {
        LOGGER.warn("Triggering cron reindex");
        indexService.reindexAll(false);
    }

    @PostConstruct
    private void postInit() {
        LOGGER.info("Started SOLR reindexing with cron \"{}\"", indexerConfiguration.getIndexingCron());
    }
}
