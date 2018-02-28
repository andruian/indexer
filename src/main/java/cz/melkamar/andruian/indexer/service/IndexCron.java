package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.DataDefDAO;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class IndexCron {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private IndexerConfiguration indexerConfiguration;
    @Autowired
    private IndexService indexService;
    @Autowired
    private DataDefDAO dataDefDAO;


    @Scheduled(cron = "${indexing.cron}")
    public void triggerReindex() {
        String[] dataDefUris = indexerConfiguration.getDataDefUris();
        for (String dataDefUri : dataDefUris) {
            DataDef dataDef = dataDefDAO.getDataDefFromUri(dataDefUri);
            if (dataDef == null){
                LOGGER.error("Could not get or parse DataDef from URL: {}", dataDefUri);
                continue;
            }
            indexService.indexDataDef(dataDef);
        }

        LOGGER.warn("Reindexing...");
    }

    @PostConstruct
    private void postInit() {
        LOGGER.info("Started SOLR reindexing with cron \"{}\"", indexerConfiguration.getIndexingCron());
    }
}
