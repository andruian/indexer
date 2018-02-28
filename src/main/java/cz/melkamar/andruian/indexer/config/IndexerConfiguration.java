package cz.melkamar.andruian.indexer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IndexerConfiguration {
    @Value("${dataDefs}")
    private String[] dataDefs;

    public String[] getDataDefUris(){
        return dataDefs;
    }
}
