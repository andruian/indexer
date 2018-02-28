package cz.melkamar.andruian.indexer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexerConfiguration {
    @Value("${dataDefs}")
    private String[] dataDefs;

    @Value("${indexing.cron}")
    private String indexingCron;

    public String[] getDataDefUris(){
        return dataDefs;
    }

    public String getIndexingCron() {
        return indexingCron;
    }
}
