package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.ddfparser.exception.DataDefFormatException;
import cz.melkamar.andruian.ddfparser.exception.RdfFormatException;
import cz.melkamar.andruian.ddfparser.model.DataDef;
import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.MongoDataDefFileRepository;
import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.model.DataDefFile;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Service
public class IndexService {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    private final IndexerConfiguration indexerConfiguration;
    private final DataDefFetcher dataDefFetcher;
    private final PlaceDAO placeDAO;
    private final MongoDataDefFileRepository datadefFileRepository;

    private final IndexServiceAsyncCall indexServiceAsyncCall;

    private final Map<DataDef, CompletableFuture> indexingJobs;

    @Autowired
    public IndexService(IndexerConfiguration indexerConfiguration,
                        DataDefFetcher dataDefFetcher,
                        PlaceDAO placeDAO,
                        MongoDataDefFileRepository datadefFileRepository,
                        IndexServiceAsyncCall indexServiceAsyncCall) {
        this.indexerConfiguration = indexerConfiguration;
        this.dataDefFetcher = dataDefFetcher;
        this.placeDAO = placeDAO;
        this.datadefFileRepository = datadefFileRepository;
        this.indexServiceAsyncCall = indexServiceAsyncCall;
        indexingJobs = new HashMap<>();
    }

    /**
     * Index data defined by the given DataDef object.
     * <p>
     * Build a SPARQL query based on the {@link DataDef} and run this query on a data SPARQL controller.
     * The query will select all objects of a type defined in the {@link DataDef} and find their linked
     * location objects and position coordinates (via a federated query, those objects may be accessible through a
     * different controller - in the prototype version this will be the RÚIAN SPARQL controller).
     *
     * @param dataDef     A definition of the data.
     * @param fullReindex If true, reindex everything. If false, skip querying of objects already indexed (incremental
     *                    reindex).
     * @return TODO: maybe no return value is even necessary?
     */
    public void indexDataDef(DataDef dataDef, boolean fullReindex) {
        CompletableFuture future = indexingJobs.get(dataDef);
        if (future != null) {
            future.cancel(true);
        }

        indexingJobs.put(dataDef, indexServiceAsyncCall.indexDataDefAsync(dataDef, fullReindex));
    }

    public List<DataDef> getRunningJobs() {
        List<DataDef> result = new ArrayList<>();
        for (Map.Entry<DataDef, CompletableFuture> futureEntry : indexingJobs.entrySet()) {
            try {
                if (futureEntry.getValue().getNow(null) == null) result.add(futureEntry.getKey());
            } catch (Exception  e) {
                LOGGER.trace("getRunningJobs - " + futureEntry.getKey() + " ended with exception " + e.getMessage());
            }
        }

        LOGGER.trace("getRunningJobs - " + result.size() + " running.");
        return result;
    }

    public static class FinishedJobReport {
        public final DataDef dataDef;
        public final int indexedCount;

        public FinishedJobReport(DataDef dataDef, int indexedCount) {
            this.dataDef = dataDef;
            this.indexedCount = indexedCount;
        }
    }

    public List<FinishedJobReport> getFinishedJobs() {
        List<FinishedJobReport> result = new ArrayList<>();
        for (Map.Entry<DataDef, CompletableFuture> futureEntry : indexingJobs.entrySet()) {
            try {
                Object indexedCount = futureEntry.getValue().getNow(null);
                if (indexedCount != null){
                    result.add(new FinishedJobReport(futureEntry.getKey(), (Integer) indexedCount));
                }
            } catch (CancellationException | CompletionException e) {
                LOGGER.trace("getRunningJobs - " + futureEntry.getKey() + " ended with exception " + e.getMessage());
            }
        }

        // Remove all errors that have been reported so that they are not shown next time
        for (FinishedJobReport jobReport : result) {
            indexingJobs.remove(jobReport.dataDef);
        }

        LOGGER.trace("getFinishedJobs - " + result.size() + " finished.");
        return result;
    }

    public Map<DataDef, Exception> getErroredJobs() {
        Map<DataDef, Exception> result = new HashMap<>();

        for (Map.Entry<DataDef, CompletableFuture> futureEntry : indexingJobs.entrySet()) {
            try {
                futureEntry.getValue().getNow(null);
            } catch (CancellationException | CompletionException e) {
                LOGGER.trace("getErroredJobs - " + futureEntry.getKey() + " ended with exception " + e.getMessage());
                result.put(futureEntry.getKey(), e);
            }
        }

        // Remove all errors that have been reported so that they are not shown next time
        for (DataDef dataDef : result.keySet()) {
            indexingJobs.remove(dataDef);
        }

        LOGGER.trace("getErroredJobs - " + result.size() + " errors.");
        return result;
    }



    public void reindexAll(boolean fullReindex) {
        LOGGER.warn("Reindexing...");

        String[] dataDefUris = datadefFileRepository.findAll().stream().map(DataDefFile::getFileUrl).toArray(String[]::new);
        for (String dataDefUri : dataDefUris) {
            List<DataDef> dataDefs = null;
            try {
                dataDefs = dataDefFetcher.getDataDefsFromUri(dataDefUri);
            } catch (RdfFormatException | DataDefFormatException | IOException e) {
                LOGGER.error("Could not get or parse DataDef from URL: {}", dataDefUri);
                e.printStackTrace();
                continue;
            }
            for (DataDef dataDef : dataDefs) {
                indexDataDef(dataDef, fullReindex);
            }
        }
    }

    public void dropData(String dataDefIri) {
        placeDAO.deletePlacesOfDataDefIri(dataDefIri);
    }

    public int getIndexedPlacesCount(String dataDefIri) {
        return placeDAO.getDatadefPlacesCount(dataDefIri);
    }

    public void addDatadefFile(String dataDefFileUrl) throws RdfFormatException, IOException, DataDefFormatException {
        List<DataDef> dataDefs = dataDefFetcher.getDataDefsFromUri(dataDefFileUrl);
        datadefFileRepository.insert(new DataDefFile(dataDefFileUrl, dataDefs.stream().map(DataDef::getUri).collect(Collectors.toList())));
    }
}
