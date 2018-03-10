package cz.melkamar.andruian.indexer.controller;

import cz.melkamar.andruian.indexer.dao.PlaceDAO;
import cz.melkamar.andruian.indexer.model.place.Place;
import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DataQueryController {
    private final PlaceDAO placeDAO;

    @Autowired
    public DataQueryController(PlaceDAO placeDAO) {
        this.placeDAO = placeDAO;
    }

    @RequestMapping("/query")
    public List<Place> query(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "lat", required = false) Double latitude,
            @RequestParam(value = "long", required = false) Double longitude,
            @RequestParam(value = "r", required = false) Double radius
            ) {
        if (type == null){
            if (checkLatLongR(latitude, longitude, radius)) return placeDAO.getPlacesAroundPoint(latitude, longitude, radius);
            else return placeDAO.getAllPlaces();
        } else { // type not null
            if (checkLatLongR(latitude, longitude, radius)) return placeDAO.getPlacesAroundPointOfClass(type, latitude, longitude, radius);
            else return placeDAO.getPlacesOfClass(type);
        }
    }
    
    private boolean checkLatLongR(Double latitude, Double longitude, Double radius){
        if (latitude==null &&longitude==null && radius == null) return false;
        if (latitude!=null &&longitude!=null && radius != null) return true;
        throw new ValueException("All of 'lat', 'long' and 'r' must be either provided or not provided.");
    }

//    @RequestMapping("/config")
//    public String[] config(){
////        fakeDataRepo.getConfig().forEach(System.out::println);
//        return fakeDataRepo.getConfig();
//    }
}
