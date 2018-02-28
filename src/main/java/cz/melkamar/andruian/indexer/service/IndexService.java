package cz.melkamar.andruian.indexer.service;

import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import org.springframework.stereotype.Service;

@Service
public class IndexService {
    public void indexDataDef(DataDef dataDef){
        throw new NotImplementedException();
    }
}
