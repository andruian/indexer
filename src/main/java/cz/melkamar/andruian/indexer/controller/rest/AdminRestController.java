package cz.melkamar.andruian.indexer.controller.rest;

import cz.melkamar.andruian.ddfparser.exception.DataDefFormatException;
import cz.melkamar.andruian.ddfparser.exception.RdfFormatException;
import cz.melkamar.andruian.ddfparser.model.DataDef;
import cz.melkamar.andruian.indexer.dao.MongoDataDefFileRepository;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
import cz.melkamar.andruian.indexer.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(value = "/api/admin")
public class AdminRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRestController.class);

    private final DataDefFetcher dataDefFetcher;
    private final IndexService indexService;
    private final MongoDataDefFileRepository mongoDataDefFileRepository;

    @Autowired
    public AdminRestController(DataDefFetcher dataDefFetcher,
                               IndexService indexService, MongoDataDefFileRepository mongoDataDefFileRepository) {
        this.dataDefFetcher = dataDefFetcher;
        this.indexService = indexService;
        this.mongoDataDefFileRepository = mongoDataDefFileRepository;
    }


    @PostMapping("reindex")
    public String reindex(@RequestParam(value = "dataDefUri", required = false, defaultValue = "") String dataDefUri,
                          @RequestParam(value = "fullReindex", required = false, defaultValue = "0") boolean fullReindex) {
        LOGGER.info("Reindex uri: '{}'. Full reindex: {}", dataDefUri, fullReindex);

        if (dataDefUri != null && dataDefUri.length() > 0) {

            try {
                List<DataDef> dataDefs = dataDefFetcher.getDataDefsFromUri(dataDefUri);
                for (DataDef dataDef : dataDefs) {
                    indexService.indexDataDef(dataDef, fullReindex);
                }
                return "Refreshing " + dataDefUri;
            } catch (RdfFormatException | DataDefFormatException | IOException e) {
                e.printStackTrace();
                return e.toString();
            }
        } else {
            // TODO Display GUI?
            // TODO at least list available URIs
            indexService.reindexAll(fullReindex);
            return "Refreshing all.";
        }
    }

    @RequestMapping("datadefs")
    public Object listDatadefs() {
        return mongoDataDefFileRepository.findAll();
    }
}
