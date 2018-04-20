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

/**
 * A helper class for fetching a remote RDF file and parsing it into POJOs.
 *
 * The <a href="https://github.com/andruian/datadef-parser">ddfparser library</a> is used to parse the file.
 */
@Component
public class DataDefFetcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataDefFetcher.class);
    private final NetHelper netHelper;

    @Autowired
    public DataDefFetcher(NetHelper netHelper) {
        this.netHelper = netHelper;
    }

    public List<DataDef> getDataDefsFromUri(String uri) throws DataDefFormatException, IOException, RdfFormatException {
        LOGGER.info("Fetching DataDef from {}", uri);
        
        DataDefParser parser = new DataDefParser();
        String payload = netHelper.httpGet(uri);
        return parser.parse(payload, RDFFormat.TURTLE);
    }
}
