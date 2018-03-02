package cz.melkamar.andruian.indexer.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@Configuration
@EnableSolrRepositories(basePackages = "cz.melkamar.andruian.indexer.dao")
@ComponentScan
public class SolrConfig {
    private final IndexerConfiguration indexerConfiguration;

    @Autowired
    public SolrConfig(IndexerConfiguration indexerConfiguration) {
        this.indexerConfiguration = indexerConfiguration;
    }


    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder(indexerConfiguration.getDbSolrUri()).build();
    }

    @Bean
    public SolrTemplate solrTemplate(SolrClient client) throws Exception {
        return new SolrTemplate(client);
    }
}
