package cz.melkamar.andruian.indexer.rdf;

import cz.melkamar.andruian.indexer.net.RdfFetcher;
import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataDefParserFactory {
    private final RdfFetcher fetcher;
    
    @Autowired
    public DataDefParserFactory(RdfFetcher fetcher) {
        this.fetcher = fetcher;
    }

    public DataDefParser createParser(Model model){
        return new DataDefParser(model, fetcher);
    }
}
