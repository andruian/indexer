package cz.melkamar.andruian.indexer.controller.ui;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.controller.Util;
import cz.melkamar.andruian.indexer.exception.QueryFormatException;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Show map with pointers.
 */
@Controller
public class MapController {
    private final QueryService queryService;
    private final IndexerConfiguration indexerConfiguration;

    @Autowired
    public MapController(QueryService queryService,
                         IndexerConfiguration indexerConfiguration) {
        this.queryService = queryService;
        this.indexerConfiguration = indexerConfiguration;
    }

    @ModelAttribute
    public void addAttributes(Model model){
        Util.addPrincipalAttribute(model);
    }

    @GetMapping("/")
    public String mainPage(Model model){
        model.addAttribute("queryAttrs", new QueryAttrs(null, null, null, null));
        model.addAttribute("errors", new Errors());
        model.addAttribute("module", "map");
        return "map";
    }

    @GetMapping("/show")
    public String showMap(@RequestParam(value = "type", required = false) String type,
                          @RequestParam(value = "lat", required = false) Double latitude,
                          @RequestParam(value = "long", required = false) Double longitude,
                          @RequestParam(value = "r", required = false) Double radius,
                          Model model) {

        Errors errors = new Errors();

        try {
            List<Place> places = queryService.query(type, latitude, longitude, radius);
            List<Loc> l = places.stream().limit(indexerConfiguration.getUiMaxPointsShown())
                    .map(place -> new Loc(place.getLatPos(), place.getLongPos()))
                    .collect(Collectors.toList());
            model.addAttribute("points", l);
            model.addAttribute("totalPointsFound", places.size());

        } catch (QueryFormatException e) {
            e.printStackTrace();
            errors.query = "An error occurred when executing the query. " + e.getMessage();
        }
        if (latitude != null && longitude != null) model.addAttribute("pos", new Loc(latitude, longitude));
        model.addAttribute("queryAttrs", new QueryAttrs(type, latitude, longitude, radius));
        model.addAttribute("errors", errors);
        model.addAttribute("module", "map");
        
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

    public class Errors {
        private String query;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }
    }
}

