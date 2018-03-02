package cz.melkamar.andruian.indexer.connector;

import cz.melkamar.andruian.indexer.model.SolrPlace;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SolrConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(SolrConnector.class);
    
    public void indexPlaces(String solrUri, String collection, List<SolrPlace> solrPlaces)
            throws IOException, SolrServerException {
        LOGGER.debug("Indexing {} places at {} into collection '{}'", solrPlaces.size(), solrUri, collection);
        
        SolrClient client = new HttpSolrClient.Builder(solrUri).build();
        for (SolrPlace solrPlace : solrPlaces) client.addBean(collection, solrPlace);
        client.commit(collection);
    }

    private List<SolrPlace> query(String solrUri, String collection, SolrParams params)
            throws IOException, SolrServerException {
        LOGGER.debug("Querying Solr at {}; collection '{}' with params {}", solrUri, collection, params);
        SolrClient client = new HttpSolrClient.Builder(solrUri).build();
        QueryResponse response = client.query(collection, params);

        return response.getBeans(SolrPlace.class);
    }
}
