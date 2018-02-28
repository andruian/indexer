package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class IndexService {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    @Async
    public CompletableFuture indexDataDef(DataDef dataDef){
        LOGGER.info("Indexing data from DataDef {}", dataDef.getUri());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        LOGGER.info("Finished indexing {}", dataDef.getUri());
        return CompletableFuture.completedFuture(null);
    }
}
