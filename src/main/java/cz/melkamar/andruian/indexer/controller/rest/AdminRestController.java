package cz.melkamar.andruian.indexer.controller.rest;

import cz.melkamar.andruian.ddfparser.exception.DataDefFormatException;
import cz.melkamar.andruian.ddfparser.exception.RdfFormatException;
import cz.melkamar.andruian.ddfparser.model.DataDef;
import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.Property;
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
import java.util.Random;

@RestController
@RequestMapping(value = "/api/admin")
public class AdminRestController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRestController.class);

    private final DataDefFetcher dataDefFetcher;
    private final IndexService indexService;
    private final PlaceDAO placeDao;
    private final IndexerConfiguration configuration;
    private final Random random = new Random();

    @Autowired
    public AdminRestController(DataDefFetcher dataDefFetcher,
                               IndexService indexService,
                               PlaceDAO placeDao, IndexerConfiguration configuration) {
        this.dataDefFetcher = dataDefFetcher;
        this.indexService = indexService;
        this.placeDao = placeDao;
        this.configuration = configuration;
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
            } catch (RdfFormatException | DataDefFormatException |IOException e) {
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
    public Object listDatadefs(){
        return configuration.getDataDefUris();
    }

    @RequestMapping("addFakeData")
    public String addFakeData() {
        int dataCount = 10;
        for (int i = 0; i < dataCount; i++) {
            Double latitude = 50 + (random.nextDouble() * 2 - 1);
            Double longitude = 12 + (random.nextDouble() * 2 - 1);

            Property[] properties = new Property[random.nextInt(5)];
            for (int j = 0; j < properties.length; j++) {
                properties[j] = new Property("propName" + random.nextInt(10), "val" + random.nextInt(500));
            }

            Place place = new Place(latitude,
                                    longitude,
                                    "uri" + random.nextInt(999999999),
                                    "classUri" + random.nextInt(5),
                                    "locUri" + random.nextInt(5),
                                    properties);
            placeDao.savePlace(place);
        }

        return "Added " + dataCount + " data points";
    }
}
