package cz.melkamar.andruian.indexer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexerConfiguration {
    public static final String DATADEFS = "dataDefs";
    public static final String INDEX_CRON = "indexing.cron";

    @Value("${" + DATADEFS + "}")
    private String[] dataDefs;

    @Value("${" + INDEX_CRON + ":#{null}}")
    private String indexingCron;

    public String[] getDataDefUris() {
        return dataDefs;
    }

    public String getIndexingCron() {
        return indexingCron;
    }
}
