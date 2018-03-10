package cz.melkamar.andruian.indexer.controller;

import cz.melkamar.andruian.indexer.model.place.Place;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Show map with pointers.
 */
@Controller
public class MapController {


    @Autowired
    private DataQueryController queryController;

    @GetMapping("/")
    public String showMap(@RequestParam(value = "type", required = false) String type,
                          @RequestParam(value = "lat", required = false) Double latitude,
                          @RequestParam(value = "long", required = false) Double longitude,
                          @RequestParam(value = "r", required = false) Double radius,
                          Model model) {
        List<Place> places = (List<Place>) queryController.query(type,
                                                                 latitude,
                                                                 longitude,
                                                                 radius, false);
        List<Loc> l = places.stream()
                .map(place -> new Loc(place.getLatPos(), place.getLongPos()))
                .collect(Collectors.toList());
        model.addAttribute("points", l);
        model.addAttribute("queryAttrs", new QueryAttrs(
                type,
                latitude,
                longitude,
                radius));
        return "map";
    }


    class Loc {
        public double lat;
        public double lng;

        public Loc(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }
    }

    class QueryAttrs {
        public String type;
        public Double lat;
        public Double longitude;
        public Double radius;

        public QueryAttrs() {
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getLat() {
            return lat;
        }

        public void setLat(Double lat) {
            this.lat = lat;
        }

        public Double getLong() {
            return longitude;
        }

        public void setLong(Double longitude) {
            this.longitude = longitude;
        }

        public Double getR() {
            return radius;
        }

        public void setR(Double radius) {
            this.radius = radius;
        }

        QueryAttrs(String type, Double lat, Double longitude, Double radius) {
            this.type = type;
            this.lat = lat;
            this.longitude = longitude;
            this.radius = radius;
        }
    }
}

