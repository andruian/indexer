package cz.melkamar.andruian.indexer.controller.ui;

import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.controller.Util;
import cz.melkamar.andruian.indexer.exception.QueryFormatException;
import cz.melkamar.andruian.indexer.model.place.Place;
import cz.melkamar.andruian.indexer.model.place.PlaceCluster;
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
 * A controller for the map view page.
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
    public void addAttributes(Model model) {
        Util.addPrincipalAttribute(model);
    }

    /**
     * An endpoint method for showing the map page without any objects in it.
     */
    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("queryAttrs", new QueryAttrs(null, null, null, null, false));
        model.addAttribute("errors", new Errors());
        model.addAttribute("module", "map");
        return "map";
    }

    /**
     * An endpoint method for showing the map and objects according to a search query. The query parameters are
     * almost identical to those of {@link cz.melkamar.andruian.indexer.controller.rest.DataQueryRestController#query(String, Double, Double, Double, boolean, Integer)}.
     *
     * @param type
     * @param latitude
     * @param longitude
     * @param radius
     * @param cluster If true, cluster results. If false, show markers for all results without clustering.
     * @param model
     * @return
     */
    @GetMapping("/show")
    public String showMap(@RequestParam(value = "type", required = false) String type,
                          @RequestParam(value = "lat", required = false) Double latitude,
                          @RequestParam(value = "long", required = false) Double longitude,
                          @RequestParam(value = "r", required = false) Double radius,
                          @RequestParam(value = "cluster", required = false) Boolean cluster,
                          Model model) {

        Errors errors = new Errors();

        try {
            List<Loc> l = null;

            if (cluster == null || !cluster) {
                List<Place> places = queryService.query(type, latitude, longitude, radius);
                l = places.stream().limit(indexerConfiguration.getUiMaxPointsShown())
                        .map(place -> new Loc(place.getLatPos(), place.getLongPos()))
                        .collect(Collectors.toList());
                model.addAttribute("totalPointsFound", places.size());
            } else {
                List<PlaceCluster> clusters = queryService.clusteredQuery(type, latitude, longitude, radius);
                l = clusters.stream()
                        .map(placeCluster -> new Loc(placeCluster.getLatPos(), placeCluster.getLongPos()))
                        .collect(Collectors.toList());

                model.addAttribute("totalPointsFound", clusters.size());
            }

            model.addAttribute("points", l);

        } catch (QueryFormatException e) {
            e.printStackTrace();
            errors.query = "An error occurred when executing the query. " + e.getMessage();
        }
        if (latitude != null && longitude != null) model.addAttribute("pos", new Loc(latitude, longitude));
        model.addAttribute("queryAttrs", new QueryAttrs(type, latitude, longitude, radius, cluster));
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

    /**
     * A wrapper class for all the data filled into the form for searching through objects and showing them on the map.
     */
    class QueryAttrs {
        public String type;
        public Double lat;
        public Double longitude;
        public Double radius;
        public Boolean cluster;

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

        public Boolean getCluster() {
            return cluster;
        }

        public void setCluster(Boolean cluster) {
            this.cluster = cluster;
        }

        QueryAttrs(String type, Double lat, Double longitude, Double radius, Boolean cluster) {
            this.type = type;
            this.lat = lat;
            this.longitude = longitude;
            this.radius = radius;
            this.cluster = cluster;
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

