package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.ddfparser.model.ClassToLocPath;
import cz.melkamar.andruian.ddfparser.model.DataDef;
import cz.melkamar.andruian.ddfparser.model.SelectProperty;
import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.exception.DataDefIndexException;
import cz.melkamar.andruian.indexer.exception.SparqlQueryException;
import cz.melkamar.andruian.indexer.model.place.Place;
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
public class IndexServiceAsyncCall {
    private final static Logger LOGGER = LoggerFactory.getLogger(IndexService.class);
    private final PlaceDAO placeDAO;
    private final SparqlConnector sparqlConnector;

    @Autowired
    public IndexServiceAsyncCall(PlaceDAO placeDAO, SparqlConnector sparqlConnector) {
        this.placeDAO = placeDAO;
        this.sparqlConnector = sparqlConnector;
    }


    @Async
    protected CompletableFuture indexDataDefAsync(DataDef dataDef, boolean fullReindex) {
        // TODO during fullReindex delete objects that no longer exist from Mongo+Solr
        LOGGER.info("Indexing data from DataDef {}. Full reindex: {}", dataDef.getUri(), fullReindex);

        ClassToLocPath classToLocPath = dataDef.getLocationClassDef().getPathToGps(dataDef.getLocationClassDef().getClassUri());
        if (classToLocPath == null){
            throw new DataDefIndexException("ClassToLocPath not found for class "+dataDef.getLocationClassDef().getClassUri());
        }

        // Indexing stuff here
        IndexSparqlQueryBuilder queryBuilder = new IndexSparqlQueryBuilder(
                dataDef.getSourceClassDef().getClassUri(),
                dataDef.getSourceClassDef().getPathToLocationClass(),
                dataDef.getLocationClassDef().getSparqlEndpoint(),
                classToLocPath.getLatCoord(),
                classToLocPath.getLongCoord()
        );

        for (SelectProperty selectProperty : dataDef.getSourceClassDef().getSelectProperties()) {
            queryBuilder.addSelectProperty(selectProperty);
        }

        if (!fullReindex) {
            for (Place place : placeDAO.getPlacesOfClass(dataDef.getSourceClassDef().getClassUri())) {
                queryBuilder.excludeUri(place.getIri());
            }
        }


        String query = queryBuilder.build();
        LOGGER.debug("Query string: \n{}", query);
        List<Place> places = null;
        try {
            places = sparqlConnector.executeIndexQuery(dataDef.getUri(),
                                                       query,
                                                       dataDef.getSourceClassDef().getSparqlEndpoint(),
                                                       dataDef.getSourceClassDef().getSelectPropertiesNames());
        } catch (SparqlQueryException e) {
            LOGGER.error("An error occurred while performing a SPARQL query on endpoint " + dataDef.getSourceClassDef()
                    .getSparqlEndpoint(), e);
            e.printStackTrace();
            throw new DataDefIndexException(e.getMessage());
        }

        LOGGER.info("Fetched {} places from DataDef at {}:", places.size(), dataDef.getUri());

        placeDAO.savePlaces(places);

        LOGGER.info("Finished indexing {}", dataDef.getUri());
        return CompletableFuture.completedFuture(places.size());
    }
}
