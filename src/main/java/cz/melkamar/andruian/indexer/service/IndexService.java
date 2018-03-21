package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.ddfparser.exception.DataDefFormatException;
import cz.melkamar.andruian.ddfparser.exception.RdfFormatException;
import cz.melkamar.andruian.ddfparser.model.DataDef;
import cz.melkamar.andruian.ddfparser.model.SelectProperty;
import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.MongoDataDefFileRepository;
import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.exception.SparqlQueryException;
import cz.melkamar.andruian.indexer.model.DataDefFile;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
import cz.melkamar.andruian.indexer.net.SparqlConnector;
import cz.melkamar.andruian.indexer.rdf.IndexSparqlQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class IndexService {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    private final IndexerConfiguration indexerConfiguration;
    private final DataDefFetcher dataDefFetcher;
    private final SparqlConnector sparqlConnector;
    private final PlaceDAO placeDAO;
    private final MongoDataDefFileRepository datadefFileRepository;

    @Autowired
    public IndexService(IndexerConfiguration indexerConfiguration,
                        DataDefFetcher dataDefFetcher,
                        SparqlConnector sparqlConnector,
                        PlaceDAO placeDAO,
                        MongoDataDefFileRepository datadefFileRepository) {
        this.indexerConfiguration = indexerConfiguration;
        this.dataDefFetcher = dataDefFetcher;
        this.sparqlConnector = sparqlConnector;
        this.placeDAO = placeDAO;
        this.datadefFileRepository = datadefFileRepository;
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
    @Async
    public CompletableFuture indexDataDef(DataDef dataDef, boolean fullReindex) {
        // TODO during fullReindex delete objects that no longer exist from Mongo+Solr
        LOGGER.info("Indexing data from DataDef {}. Full reindex: {}", dataDef.getUri(), fullReindex);

        // Indexing stuff here
        IndexSparqlQueryBuilder queryBuilder = new IndexSparqlQueryBuilder(
                dataDef.getSourceClassDef().getClassUri(),
                dataDef.getSourceClassDef().getPathToLocationClass(),
                dataDef.getLocationClassDef().getSparqlEndpoint(),
                dataDef.getLocationClassDef().getPathToGps(dataDef.getLocationClassDef().getClassUri()).getLatCoord(),
                dataDef.getLocationClassDef().getPathToGps(dataDef.getLocationClassDef().getClassUri()).getLongCoord()
        );

        for (SelectProperty selectProperty : dataDef.getSourceClassDef().getSelectProperties()) {
            queryBuilder.addSelectProperty(selectProperty);
        }

        if (!fullReindex) {
            for (Place place : placeDAO.getPlacesOfClass(dataDef.getSourceClassDef().getClassUri())) {
                queryBuilder.excludeUri(place.getUri());
            }
        }


        String query = queryBuilder.build();
        LOGGER.debug("Query string: \n{}", query);
        List<Place> places = null;
        try {
            places = sparqlConnector.executeIndexQuery(query,
                                                       dataDef.getSourceClassDef().getSparqlEndpoint(),
                                                       dataDef.getSourceClassDef().getSelectPropertiesNames());
        } catch (SparqlQueryException e) {
            LOGGER.error("An error occurred while performing a SPARQL query on endpoint " + dataDef.getSourceClassDef()
                    .getSparqlEndpoint(), e);
            e.printStackTrace();
            return CompletableFuture.completedFuture(null);
        }

        LOGGER.info("Indexed {} places from DataDef at {}:", places.size(), dataDef.getUri());
        for (Place place : places) {
            LOGGER.debug(place.toString());
        }

        placeDAO.savePlaces(places);

        LOGGER.info("Finished indexing {}", dataDef.getUri());
        return CompletableFuture.completedFuture(null);
    }

    public void reindexAll(boolean fullReindex) {
        LOGGER.warn("Reindexing...");

        String[] dataDefUris = indexerConfiguration.getDataDefUris();
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

    public void addDatadef(String dataDefUri) {
        datadefFileRepository.insert(new DataDefFile(dataDefUri));
    }
}
