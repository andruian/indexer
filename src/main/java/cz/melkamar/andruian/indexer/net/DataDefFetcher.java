package cz.melkamar.andruian.indexer.net;

import cz.melkamar.andruian.ddfparser.DataDefParser;
import cz.melkamar.andruian.ddfparser.exception.DataDefFormatException;
import cz.melkamar.andruian.ddfparser.exception.RdfFormatException;
import cz.melkamar.andruian.ddfparser.model.DataDef;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class DataDefFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataDefFetcher.class);
    private final RdfFetcher rdfFetcher;

    @Autowired
    public DataDefFetcher(RdfFetcher rdfFetcher) {
        this.rdfFetcher = rdfFetcher;
    }

    public List<DataDef> getDataDefsFromUri(String uri) throws DataDefFormatException, IOException, RdfFormatException {
        LOGGER.info("Fetching DataDef from {}", uri);
        
        DataDefParser parser = new DataDefParser();
        String payload = rdfFetcher.getDataDefFromUri(uri);
        return parser.parse(payload, RDFFormat.TURTLE);
    }
}
