package cz.melkamar.andruian.indexer.dao.store;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.connector.SolrConnector;
import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.exception.DAOException;
import cz.melkamar.andruian.indexer.model.SolrPlace;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class SolrDAO {
    private SolrConnector solrConnector;
    private IndexerConfiguration indexerConfiguration;
    
    private static final int LIMIT_QUERY_ROWS = 100; // TODO make configurable

    @Autowired
    public SolrDAO(SolrConnector solrConnector,
                   IndexerConfiguration indexerConfiguration) {
        this.solrConnector = solrConnector;
        this.indexerConfiguration = indexerConfiguration;
    }

    public void saveSolrPlaces(List<SolrPlace> solrPlaces) throws DAOException {
        try {
            solrConnector.indexPlaces(indexerConfiguration.getDbSolrUri(),
                                      indexerConfiguration.getDbSolrCollection(),
                                      solrPlaces);
        } catch (IOException | SolrServerException e) {
            e.printStackTrace();
            throw new DAOException(e);
        }
    }

    public void deleteSolrPlace(SolrPlace solrPlace) {
        throw new NotImplementedException();
    }

    public List<SolrPlace> getAllSolrPlaces() {
        throw new NotImplementedException();
    }

    public List<SolrPlace> getSolrPlacesOfClass(String classUri) {
        throw new NotImplementedException();
    }

    public List<SolrPlace> getSolrPlacesAroundPoint(double latCoord, double longCoord, double radius) {
        throw new NotImplementedException();
    }

    public List<SolrPlace> getAllSolrPlaces(String classUri, double latCoord, double longCoord, double radius) {
        throw new NotImplementedException();
    }
}
