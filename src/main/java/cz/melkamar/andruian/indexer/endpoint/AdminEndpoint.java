package cz.melkamar.andruian.indexer.endpoint;

import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.Property;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
import cz.melkamar.andruian.indexer.service.IndexService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping(value = "/admin")
public class AdminEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminEndpoint.class);

    private final DataDefFetcher dataDefFetcher;
    private final IndexService indexService;
    private final PlaceDAO placeDao;
    private final Random random = new Random();

    @Autowired
    public AdminEndpoint(DataDefFetcher dataDefFetcher,
                         IndexService indexService,
                         PlaceDAO placeDao) {
        this.dataDefFetcher = dataDefFetcher;
        this.indexService = indexService;
        this.placeDao = placeDao;
    }


    @RequestMapping("reindex")
    public String reindex(@RequestParam(value = "dataDefUri", required = false, defaultValue = "") String dataDefUri,
                          @RequestParam(value = "fullReindex", required = false, defaultValue = "0") boolean fullReindex) {
        LOGGER.info("Reindex uri: '{}'. Full reindex: {}", dataDefUri, fullReindex);

        if (dataDefUri != null && dataDefUri.length() > 0) {
            DataDef dataDef = dataDefFetcher.getDataDefFromUri(dataDefUri);
            indexService.indexDataDef(dataDef, fullReindex);
            return "Refreshing " + dataDefUri;
        } else {
            // TODO Display GUI?
            // TODO at least list available URIs
            indexService.reindexAll(fullReindex);
            return "Refreshing all.";
        }
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
