package cz.melkamar.andruian.indexer.net;

import cz.melkamar.andruian.indexer.exception.DataDefFormatException;
import cz.melkamar.andruian.indexer.exception.RdfFormatException;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.rdf.DataDefParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataDefFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataDefFetcher.class);
    private final RdfFetcher rdfFetcher;

    @Autowired
    public DataDefFetcher(RdfFetcher rdfFetcher) {
        this.rdfFetcher = rdfFetcher;
    }

    public DataDef getDataDefFromUri(String uri) throws RdfFormatException, DataDefFormatException {
        LOGGER.info("Fetching DataDef from {}", uri);
        
        DataDefParser parser = new DataDefParser(rdfFetcher.getDataDefFromUri(uri), rdfFetcher);
        return parser.parse();
    }
}
