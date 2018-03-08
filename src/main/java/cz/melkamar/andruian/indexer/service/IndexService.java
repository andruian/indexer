package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.exception.DataDefFormatException;
import cz.melkamar.andruian.indexer.exception.RdfFormatException;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.model.datadef.SelectProperty;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.SolrPlace;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
import cz.melkamar.andruian.indexer.net.SparqlConnector;
import cz.melkamar.andruian.indexer.rdf.IndexSparqlQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class IndexService {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexService.class);

    private final IndexerConfiguration indexerConfiguration;
    private final DataDefFetcher dataDefFetcher;
    private final SparqlConnector sparqlConnector;
    private final PlaceDAO placeDAO;

    @Autowired
    public IndexService(IndexerConfiguration indexerConfiguration,
                        DataDefFetcher dataDefFetcher,
                        SparqlConnector sparqlConnector,
                        PlaceDAO placeDAO) {
        this.indexerConfiguration = indexerConfiguration;
        this.dataDefFetcher = dataDefFetcher;
        this.sparqlConnector = sparqlConnector;
        this.placeDAO = placeDAO;
    }

    /**
     * Index data defined by the given DataDef object.
     * <p>
     * Build a SPARQL query based on the {@link DataDef} and run this query on a data SPARQL endpoint.
     * The query will select all objects of a type defined in the {@link DataDef} and find their linked
     * location objects and position coordinates (via a federated query, those objects may be accessible through a
     * different endpoint - in the prototype version this will be the RÚIAN SPARQL endpoint).
     * <p>
     * Objects obtained by the query will be stored in Solr and MongoDB. Solr will only contain a stub of the
     * full representation - {@link SolrPlace}). This stub will contain location coordinates and a URI of the
     * resource. The whole representation - {@link Place} will be stored in MongoDB with the resource URI as the key.
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
                dataDef.getLocationDef().getSparqlEndpoint(),
                dataDef.getLocationDef().getPathToGps(dataDef.getLocationDef().getClassUri()).getLatCoord(),
                dataDef.getLocationDef().getPathToGps(dataDef.getLocationDef().getClassUri()).getLongCoord()
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
        List<Place> places = sparqlConnector.executeIndexQuery(query,
                                                               dataDef.getSourceClassDef().getSparqlEndpoint(),
                                                               dataDef.getSourceClassDef().getSelectPropertiesNames());

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
            DataDef dataDef = null;
            try {
                dataDef = dataDefFetcher.getDataDefFromUri(dataDefUri);
            } catch (RdfFormatException | DataDefFormatException e) {
                LOGGER.error("Could not get or parse DataDef from URL: {}", dataDefUri);
                e.printStackTrace();
                continue;
            }

            indexDataDef(dataDef, fullReindex);
        }
    }
}
