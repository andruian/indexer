package cz.melkamar.andruian.indexer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexerConfiguration {
    public static final String DATADEFS = "dataDefs";
    public static final String INDEX_CRON = "indexing.cron";
    public static final String ONSTART_REINDEX = "onstart.reindex";

    public static final String DB_SOLR_URL = "db.solr.url";
    public static final String DB_SOLR_COLLECTION = "db.solr.collection";

    @Value("${" + DATADEFS + "}")
    private String[] dataDefs;

    @Value("${" + INDEX_CRON + ":#{null}}")
    private String indexingCron;

    @Value("${" + ONSTART_REINDEX + ":#{false}}")
    private boolean onStartReindex;

    @Value("${" + DB_SOLR_URL + "}")
    private String dbSolrUri;
    @Value("${" + DB_SOLR_COLLECTION + "}")
    private String dbSolrCollection;

    public String[] getDataDefUris() {
        return dataDefs;
    }

    public String getIndexingCron() {
        return indexingCron;
    }

    public boolean isOnStartReindex() {
        return onStartReindex;
    }

    public String getDbSolrUri() {
        return dbSolrUri;
    }

    public String getDbSolrCollection() {
        return dbSolrCollection;
    }
}
