package cz.melkamar.andruian.indexer;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties
public class Config {
    private List<String> dataDefs = new ArrayList<>();

    public List<String> getDataDefs() {
        return dataDefs;
    }
}
