package cz.melkamar.andruian.indexer.endpoint;

import cz.melkamar.andruian.indexer.dao.DataDefDAO;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.service.IndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/admin")
public class AdminEndpoint {
    @Autowired
    DataDefDAO dataDefDAO;
    @Autowired
    private IndexService indexService;


    @RequestMapping("reindex")
    public String reindex(@RequestParam(value = "dataDefUri", required = false, defaultValue = "") String dataDefUri) {
        if (dataDefUri != null && dataDefUri.length() > 0) {
            DataDef dataDef = dataDefDAO.getDataDef(dataDefUri);
            indexService.indexDataDef(dataDef);
            return "Refreshing " + dataDefUri;
        } else {
            // TODO Display GUI?
            // TODO at least list available URIs
            return "Expected a dataDefUri parameter.";
        }
    }
}
