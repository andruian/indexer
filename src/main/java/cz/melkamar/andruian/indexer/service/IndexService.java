package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.DataDefDAO;
import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.dao.SolrPlaceRepository;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.model.datadef.SelectProperty;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.SolrPlace;
import cz.melkamar.andruian.indexer.rdf.SparqlConnector;
import cz.melkamar.andruian.indexer.rdf.sparql.IndexQueryBuilder;
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
    private final DataDefDAO dataDefDAO;
    private final SparqlConnector sparqlConnector;
    private final SolrPlaceRepository solrPlaceRepository;
    private final PlaceDAO placeDAO;

    @Autowired
    public IndexService(IndexerConfiguration indexerConfiguration,
                        DataDefDAO dataDefDAO,
                        SparqlConnector sparqlConnector,
                        SolrPlaceRepository solrPlaceRepository, PlaceDAO placeDAO) {
        this.indexerConfiguration = indexerConfiguration;
        this.dataDefDAO = dataDefDAO;
        this.sparqlConnector = sparqlConnector;
        this.solrPlaceRepository = solrPlaceRepository;
        this.placeDAO = placeDAO;
    }

    @Async
    public CompletableFuture indexDataDef(DataDef dataDef) {
        LOGGER.info("Indexing data from DataDef {}", dataDef.getUri());

        // Indexing stuff here
        IndexQueryBuilder queryBuilder = new IndexQueryBuilder(
                dataDef.getDataClassDef().getClassUri(),
                dataDef.getDataClassDef().getPathToLocationClass(),
                dataDef.getLocationDef().getSparqlEndpoint(),
                dataDef.getLocationDef().getPathToGps(dataDef.getLocationDef().getClassUri()).getLatCoord(),
                dataDef.getLocationDef().getPathToGps(dataDef.getLocationDef().getClassUri()).getLongCoord()
        );

        for (SelectProperty selectProperty : dataDef.getDataClassDef().getSelectProperties()) {
            queryBuilder.addSelectProperty(selectProperty);
        }

        // TODO allow forcing full reindex
        for (SolrPlace solrPlace : solrPlaceRepository.findByType(dataDef.getDataClassDef().getClassUri())) {
            queryBuilder.excludeUri(solrPlace.getUri());
        }


        String query = queryBuilder.build();
        LOGGER.debug("Query string: \n{}", query);
        List<Place> places = sparqlConnector.executeIndexQuery(query,
                                                               dataDef.getDataClassDef().getSparqlEndpoint(),
                                                               dataDef.getDataClassDef().getSelectPropertiesNames());

        LOGGER.info("Indexed {} places from DataDef at {}:", places.size(), dataDef.getUri());
        for (Place place : places) {
            LOGGER.debug(place.toString());
        }

        placeDAO.savePlaces(places);

        LOGGER.info("Finished indexing {}", dataDef.getUri());
        return CompletableFuture.completedFuture(null);
    }

    public void reindexAll() {
        LOGGER.warn("Reindexing...");

        String[] dataDefUris = indexerConfiguration.getDataDefUris();
        for (String dataDefUri : dataDefUris) {
            DataDef dataDef = dataDefDAO.getDataDefFromUri(dataDefUri);
            if (dataDef == null) {
                LOGGER.error("Could not get or parse DataDef from URL: {}", dataDefUri);
                continue;
            }
            indexDataDef(dataDef);
        }
    }
}
