package cz.melkamar.andruian.indexer.controller;

import cz.melkamar.andruian.indexer.exception.QueryFormatException;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DataQueryController {
    private final QueryService queryService;

    @Autowired
    public DataQueryController(QueryService queryService) {
        this.queryService = queryService;
    }

    @RequestMapping("/query")
    public Object query(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "lat", required = false) Double latitude,
            @RequestParam(value = "long", required = false) Double longitude,
            @RequestParam(value = "r", required = false) Double radius,
            @RequestParam(value = "count", required = false) boolean showCount
            ) {
        try {
            List<Place> places = queryService.query(type, latitude, longitude, radius);
            if (showCount) return places.size();
            else return places;
        } catch (QueryFormatException e) {
            e.printStackTrace();
            return e.toString(); // TODO show error page?
        }
    }
    
    

//    @RequestMapping("/config")
//    public String[] config(){
////        fakeDataRepo.getConfig().forEach(System.out::println);
//        return fakeDataRepo.getConfig();
//    }
}
