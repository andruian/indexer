package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.DataDefDAO;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
//@ConditionalOnProperty(IndexerConfiguration.INDEX_CRON)
public class IndexCron {
    private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    private final IndexerConfiguration indexerConfiguration;
    private final IndexService indexService;
    private final DataDefDAO dataDefDAO;

    @Autowired
    public IndexCron(IndexerConfiguration indexerConfiguration,
                     IndexService indexService,
                     DataDefDAO dataDefDAO) {
        this.indexerConfiguration = indexerConfiguration;
        this.indexService = indexService;
        this.dataDefDAO = dataDefDAO;
    }

    @Scheduled(cron = "${" + IndexerConfiguration.INDEX_CRON + "}")
    public void triggerReindex() {
        String[] dataDefUris = indexerConfiguration.getDataDefUris();
        for (String dataDefUri : dataDefUris) {
            DataDef dataDef = dataDefDAO.getDataDefFromUri(dataDefUri);
            if (dataDef == null) {
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
