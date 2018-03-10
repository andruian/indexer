package cz.melkamar.andruian.indexer.net;

import cz.melkamar.andruian.indexer.exception.DataDefFormatException;
import cz.melkamar.andruian.indexer.exception.RdfFormatException;
import cz.melkamar.andruian.indexer.rdf.JenaUtil;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RdfFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(RdfFetcher.class);
    private final RestTemplate restTemplate;

    @Autowired
    public RdfFetcher(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Model getDataDefFromUri(String uri) throws RdfFormatException, DataDefFormatException {
        LOGGER.info("Fetching RDF data from {}", uri);

        String payload = restTemplate.getForObject(uri, String.class);
        LOGGER.trace("Downloaded payload");
        Model model = JenaUtil.modelFromString(payload);
        if (model == null) { // TODO maybe use exceptions instead of this?
            LOGGER.error("Model could not be parsed from url: {}", uri);
            throw new RdfFormatException("Model could not be parsed.");
        }
        return model;
    }
}