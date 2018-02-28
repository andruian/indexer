package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.DataDefDAO;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class IndexService {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    private final IndexerConfiguration indexerConfiguration;
    private final DataDefDAO dataDefDAO;

    @Autowired
    public IndexService(IndexerConfiguration indexerConfiguration,
                        DataDefDAO dataDefDAO) {
        this.indexerConfiguration = indexerConfiguration;
        this.dataDefDAO = dataDefDAO;
    }

    @Async
    public CompletableFuture indexDataDef(DataDef dataDef){
        LOGGER.info("Indexing data from DataDef {}", dataDef.getUri());
        
        // Indexing stuff here
        
        
        
        
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        
        
        LOGGER.info("Finished indexing {}", dataDef.getUri());
        return CompletableFuture.completedFuture(null);
    }
    
    public void reindexAll(){
        LOGGER.warn("Reindexing...");
        
        String[] dataDefUris = indexerConfiguration.getDataDefUris();
        for (String dataDefUri : dataDefUris) {
            DataDef dataDef = dataDefDAO.getDataDefFromUri(dataDefUri);
            if (dataDef == null) {
                LOGGER.error("Could not get or parse DataDef from URL: {}", dataDefUri);
                continue;
            }
            indexDataDef(dataDef);
        }
    }
}
