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
     * The indexing is run asynchronously by delegating the method call to {@link IndexServiceAsyncCall#indexDataDefAsync(DataDef, boolean)}.
     *
     * @param dataDef     A definition of the data.
     * @param fullReindex If true, reindex everything. If false, skip querying of objects already indexed (incremental
     *                    reindex).
     */
    public void indexDataDef(DataDef dataDef, boolean fullReindex) {
        CompletableFuture future = indexingJobs.get(dataDef);
        if (future != null) {
            future.cancel(true);
        }

        indexingJobs.put(dataDef, indexServiceAsyncCall.indexDataDefAsync(dataDef, fullReindex));
    }

    /**
     * Get all indexing jobs that are currently running.
     *
     * A running job is a {@link java.util.concurrent.Future} that is present in the indexingJobs collection and does
     * not give any return value (returns null). That means its execution has not ended yet.
     *
     * @return A list of {@link DataDef} objects that are currently being indexed.
     */
    public List<DataDef> getRunningJobs() {
        List<DataDef> result = new ArrayList<>();
        for (Map.Entry<DataDef, CompletableFuture> futureEntry : indexingJobs.entrySet()) {
            try {
                if (futureEntry.getValue().getNow(null) == null) result.add(futureEntry.getKey());
            } catch (Exception e) {
                LOGGER.trace("getRunningJobs - " + futureEntry.getKey() + " ended with exception " + e.getMessage());
            }
        }

        LOGGER.trace("getRunningJobs - " + result.size() + " running.");
        return result;
    }

    /**
     * A wrapper for the result of an indexing job.
     */
    public static class FinishedJobReport {
        public final DataDef dataDef;
        public final int indexedCount;

        public FinishedJobReport(DataDef dataDef, int indexedCount) {
            this.dataDef = dataDef;
            this.indexedCount = indexedCount;
        }
    }

    /**
     * Get all indexing jobs that have finished and have not yet been read. Remove all finished jobs from the pool.
     *
     * Once a job finishes, it stays in the pool and waits to be read, quite like a message in a message queue system.
     *
     * @return A list of {@link FinishedJobReport} objects describing the result of all finished jobs.
     */
    public List<FinishedJobReport> getFinishedJobs() {
        List<FinishedJobReport> result = new ArrayList<>();
        for (Map.Entry<DataDef, CompletableFuture> futureEntry : indexingJobs.entrySet()) {
            try {
                Object indexedCount = futureEntry.getValue().getNow(null);
                if (indexedCount != null) {
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

    /**
     * Get all indexing jobs that have finished with an exception. Remove all errored jobs from the pool.
     *
     * Once a job finishes, it stays in the pool and waits to be read, quite like a message in a message queue system.
     *
     * @return A map with {@link DataDef} as keys to the {@link Exception} that caused the job to fail.
     */
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


    /**
     * Reindex all data definitions that are present in the system.
     *
     * @param fullReindex If true, drop all existing data before indexing. If false, the indexing is done incrementally.
     *                    That means that the SPARQL index query will filter out IRIs of all objects that are already
     *                    indexed.
     */
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

    /**
     * Delete all indexed data of the given data definition.
     *
     * @param dataDefIri The IRI of the data definition whose data to drop.
     */
    public void dropData(String dataDefIri) {
        placeDAO.deletePlacesOfDataDefIri(dataDefIri);
    }

    /**
     * Get the number of places indexed for the given data definition.
     *
     * @param dataDefIri The IRI of the data definition for which to count its data points.
     * @return The number of places belonging to the data definition.
     */
    public int getIndexedPlacesCount(String dataDefIri) {
        return placeDAO.getDatadefPlacesCount(dataDefIri);
    }

    /**
     * Add a new data definition file to the system. The file must be a RDF file in the Turtle format.
     * The file is parsed and all data definitions it contains are added
     * to the system for indexing.
     *
     * @param dataDefFileUrl The URL pointing to the RDF file.
     * @throws RdfFormatException     When the file cannot be parsed as a RDF file.
     * @throws IOException            General read exception.
     * @throws DataDefFormatException When the file can be parsed as a RDF file, but does not conform to the structure
     *                                prescribed by the data definition schema.
     */
    public void addDatadefFile(String dataDefFileUrl) throws RdfFormatException, IOException, DataDefFormatException {
        List<DataDef> dataDefs = dataDefFetcher.getDataDefsFromUri(dataDefFileUrl);
        datadefFileRepository.insert(new DataDefFile(dataDefFileUrl, dataDefs.stream().map(DataDef::getUri).collect(Collectors.toList())));
    }
}
