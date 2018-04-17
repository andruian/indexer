package cz.melkamar.andruian.indexer.controller.rest;

import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.exception.QueryFormatException;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/query")
public class DataQueryRestController {
    private final QueryService queryService;

    @Autowired
    public DataQueryRestController(QueryService queryService) {
        this.queryService = queryService;
    }

    /**
     *
     * @param type
     * @param latitude gps
     * @param longitude gps
     * @param radius kilometers
     * @param showCount
     * @param clusterLimit
     * @return
     */
    @RequestMapping
    public Object query(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "lat", required = false) Double latitude,
            @RequestParam(value = "long", required = false) Double longitude,
            @RequestParam(value = "r", required = false) Double radius,
            @RequestParam(value = "count", required = false) boolean showCount,
            @RequestParam(value = "clusterLimit", required = false) Integer clusterLimit
            ) {
        try {
            List<Place> places = queryService.query(type, latitude, longitude, radius);
            if (showCount) return new QueryResponse(QueryResponse.RESPONSE_COUNT, places.size());

            // No clustering limit set or number of places lower than the limit
            if (clusterLimit == null || places.size() <= clusterLimit){
                return new QueryResponse(QueryResponse.RESPONSE_ALL, places);
            }

            return new QueryResponse(QueryResponse.RESPONSE_CLUSTERED, queryService);

        } catch (QueryFormatException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.toString());
        }
    }

    private Object nonClusteredQuery(){
        throw new NotImplementedException();
    }
    
    static class QueryResponse {
        public static final int RESPONSE_COUNT = 0;
        public static final int RESPONSE_ALL = 1;
        public static final int RESPONSE_CLUSTERED = 2;

        private final int responseType;
        private final Object responseBody;

        QueryResponse(int responseType, Object responseBody) {
            this.responseType = responseType;
            this.responseBody = responseBody;
        }
    }

//    @RequestMapping("/config")
//    public String[] config(){
////        fakeDataRepo.getConfig().forEach(System.out::println);
//        return fakeDataRepo.getConfig();
//    }
}
