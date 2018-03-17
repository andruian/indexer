package cz.melkamar.andruian.indexer.controller.rest;

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

    @RequestMapping
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.toString());
        }
    }
    
    

//    @RequestMapping("/config")
//    public String[] config(){
////        fakeDataRepo.getConfig().forEach(System.out::println);
//        return fakeDataRepo.getConfig();
//    }
}
