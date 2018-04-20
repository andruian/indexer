package cz.melkamar.andruian.indexer.controller.rest;

import cz.melkamar.andruian.ddfparser.exception.DataDefFormatException;
import cz.melkamar.andruian.ddfparser.exception.RdfFormatException;
import cz.melkamar.andruian.ddfparser.model.DataDef;
import cz.melkamar.andruian.indexer.Util;
import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.MongoDataDefFileRepository;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
import cz.melkamar.andruian.indexer.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping(value = "/api/admin")
public class AdminRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRestController.class);

    private final DataDefFetcher dataDefFetcher;
    private final IndexService indexService;
    private final MongoDataDefFileRepository mongoDataDefFileRepository;
    private final IndexerConfiguration indexerConfiguration;

    @Autowired
    public AdminRestController(DataDefFetcher dataDefFetcher,
                               IndexService indexService,
                               MongoDataDefFileRepository mongoDataDefFileRepository,
                               IndexerConfiguration indexerConfiguration) {
        this.dataDefFetcher = dataDefFetcher;
        this.indexService = indexService;
        this.mongoDataDefFileRepository = mongoDataDefFileRepository;
        this.indexerConfiguration = indexerConfiguration;
    }


    /**
     * An endpoint method that reindexes one or more data definitions stored in the system.
     *
     * @param dataDefUri  The URL of an RDF file which contains one or more data definitions to be reindexed.
     * @param fullReindex If true, existing data for all data definitions found in the dataDefUri will be dropped before being reindexed.
     * @return Plain string describing what happened.
     */
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
            indexService.reindexAll(fullReindex);
            return "Refreshing all.";
        }
    }

    /**
     * An endpoint method that returns the contents of the application log.
     * @return The contents of the application log wrapped in &lt;pre&gt; HTML tag.
     */
    @GetMapping("log")
    public String log() {
        try {
            String logStr = Util.readFile(indexerConfiguration.getLoggingFile(), StandardCharsets.UTF_8);
            return "<pre>" + logStr + "</pre>";
        } catch (IOException e) {
            e.printStackTrace();
            return "Log file not found.";
        }
    }

    @RequestMapping("datadefs")
    public Object listDatadefs() {
        return mongoDataDefFileRepository.findAll();
    }
}
