package cz.melkamar.andruian.indexer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexerConfiguration {
    public static final String DATADEFS = "dataDefs";
    public static final String INDEX_CRON = "indexing.cron";
    public static final String ONSTART_REINDEX = "onstart.reindex";

    @Value("${" + DATADEFS + "}")
    private String[] dataDefs;

    @Value("${" + INDEX_CRON + ":#{null}}")
    private String indexingCron;

    @Value("${" + ONSTART_REINDEX + ":#{false}}")
    private boolean onStartReindex;

    public String[] getDataDefUris() {
        return dataDefs;
    }

    public String getIndexingCron() {
        return indexingCron;
    }

    public boolean isOnStartReindex() {
        return onStartReindex;
    }
}
