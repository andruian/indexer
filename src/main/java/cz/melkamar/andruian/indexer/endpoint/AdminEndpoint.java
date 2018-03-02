package cz.melkamar.andruian.indexer.endpoint;

import cz.melkamar.andruian.indexer.dao.DataDefDAO;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/admin")
public class AdminEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminEndpoint.class);
    
    private final DataDefDAO dataDefDAO;
    private final IndexService indexService;

    @Autowired
    public AdminEndpoint(DataDefDAO dataDefDAO, IndexService indexService) {
        this.dataDefDAO = dataDefDAO;
        this.indexService = indexService;
    }


    @RequestMapping("reindex")
    public String reindex(@RequestParam(value = "dataDefUri", required = false, defaultValue = "") String dataDefUri) {
        LOGGER.info("Reindex uri: '{}'", dataDefUri);
        
        if (dataDefUri != null && dataDefUri.length() > 0) {
            DataDef dataDef = dataDefDAO.getDataDefFromUri(dataDefUri);
            indexService.indexDataDef(dataDef);
            return "Refreshing " + dataDefUri;
        } else {
            // TODO Display GUI?
            // TODO at least list available URIs
            indexService.reindexAll();
            return "Refreshing all";
        }
    }
}
