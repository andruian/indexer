package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.rdf.DataDefParser;
import cz.melkamar.andruian.indexer.rdf.JenaUtil;
import org.apache.jena.rdf.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DataDefDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataDefDAO.class);
    private final RestTemplate restTemplate;

    @Autowired
    public DataDefDAO(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public DataDef getDataDefFromUri(String uri) {
        LOGGER.info("Fetching DataDef from {}", uri);

        String payload = restTemplate.getForObject(uri, String.class);
        Model model = JenaUtil.modelFromString(payload);
        if (model == null) { // TODO maybe use exceptions instead of this?
            LOGGER.error("Model could not be parsed.");
            return null;
        }

        DataDefParser parser = new DataDefParser(model);
        return parser.parse();
    }
}
