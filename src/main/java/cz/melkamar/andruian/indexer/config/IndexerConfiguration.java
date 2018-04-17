package cz.melkamar.andruian.indexer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexerConfiguration {
    public static final String INDEX_CRON = "indexing.cron";
    public static final String ONSTART_REINDEX = "indexing.onstart";

    public static final String DB_SOLR_URL = "db.solr.url";
    public static final String DB_SOLR_COLLECTION = "db.solr.collection";

    public static final String ADMIN_USERNAME = "admin.username";
    public static final String ADMIN_PASSWORD = "admin.password";

    public static final String MAX_POINTS_SHOWN = "ui.maxPointsShown";
    public static final String LOGGING_FILE = "logging.file";

    //https://lucene.apache.org/solr/guide/6_6/spatial-search.html
    public static final String CLUSTERING_DIST_ERR_PCT = "clustering.distErrPct";

    @Value("${" + INDEX_CRON + ":#{null}}")
    private String indexingCron;

    @Value("${" + ONSTART_REINDEX + ":#{false}}")
    private boolean onStartReindex;

    @Value("${" + DB_SOLR_URL + "}")
    private String dbSolrUri;
    @Value("${" + DB_SOLR_COLLECTION + "}")
    private String dbSolrCollection;

    @Value("${" + ADMIN_USERNAME + "}")
    private String adminUsername;
    @Value("${" + ADMIN_PASSWORD + "}")
    private String adminPassword;

    @Value("${" + MAX_POINTS_SHOWN + "}")
    private int uiMaxPointsShown;

    @Value("${" + LOGGING_FILE + "}")
    private String loggingFile;

    @Value("${" + CLUSTERING_DIST_ERR_PCT + "}")
    private double clusteringDistErrPct;

    public String getLoggingFile() {
        return loggingFile;
    }

    public int getUiMaxPointsShown() {
        return uiMaxPointsShown;
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

    public String getAdminUsername() {
        return adminUsername;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public double getClusteringDistErrPct() {
        return clusteringDistErrPct;
    }
}
