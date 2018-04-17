package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.Util;
import cz.melkamar.andruian.indexer.config.IndexerConfiguration;
import cz.melkamar.andruian.indexer.model.place.PlaceCluster;
import cz.melkamar.andruian.indexer.net.NetHelper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClusteredPlaceDAO {
    private final static Logger LOGGER = LoggerFactory.getLogger(ClusteredPlaceDAO.class);

    private final NetHelper netHelper;
    private final IndexerConfiguration configuration;

    @Autowired
    public ClusteredPlaceDAO(NetHelper netHelper,
                             IndexerConfiguration configuration) {
        this.netHelper = netHelper;
        this.configuration = configuration;
    }

    public List<PlaceCluster> getAllPlaces() {
        LOGGER.debug("Cluster | Fetching all places");
        List<PlaceCluster> result = new ArrayList<>();
        String query = new ClusterQueryBuilder(configuration.getDbSolrUri(), configuration.getDbSolrCollection())
                .build();
        LOGGER.debug("Cluster | Using query: " + query);

        String response = netHelper.httpGet(query);
        return parseClusterQueryResponse(response);
    }

    public List<PlaceCluster> getPlacesOfClass(String classUri) {
        LOGGER.debug("Cluster | Fetching places of type "+classUri);
        List<PlaceCluster> result = new ArrayList<>();
        String query = new ClusterQueryBuilder(configuration.getDbSolrUri(), configuration.getDbSolrCollection())
                .setType(classUri)
                .build();
        LOGGER.debug("Cluster | Using query: " + query);

        String response = netHelper.httpGet(query);
        return parseClusterQueryResponse(response);
    }

    public List<PlaceCluster> getPlacesAroundPoint(double latCoord, double longCoord, double radius) {
        LOGGER.debug("Cluster | getPlacesAroundPoint {} {} {}", latCoord, longCoord, radius);
        List<PlaceCluster> result = new ArrayList<>();
        String query = new ClusterQueryBuilder(configuration.getDbSolrUri(), configuration.getDbSolrCollection())
                .setLocation(latCoord, longCoord, radius)
                .build();
        LOGGER.debug("Cluster | Using query: " + query);

        String response = netHelper.httpGet(query);
        return parseClusterQueryResponse(response);
    }

    public List<PlaceCluster> getPlacesAroundPointOfClass(String classUri,
                                                          double latCoord,
                                                          double longCoord,
                                                          double radius) {
        LOGGER.debug("Cluster | getPlacesAroundPointOfClass {} {} {} {}", classUri, latCoord, longCoord, radius);
        List<PlaceCluster> result = new ArrayList<>();
        String query = new ClusterQueryBuilder(configuration.getDbSolrUri(), configuration.getDbSolrCollection())
                .setLocation(latCoord, longCoord, radius)
                .setType(classUri)
                .build();
        LOGGER.debug("Cluster | Using query: " + query);

        String response = netHelper.httpGet(query);
        return parseClusterQueryResponse(response);
    }

    public List<PlaceCluster> parseClusterQueryResponse(String response) {
        List<PlaceCluster> result = new ArrayList<>();

        JSONArray locationJson = new JSONObject(response)
                .getJSONObject("facet_counts")
                .getJSONObject("facet_heatmaps")
                .getJSONArray("location");

        int columns = locationJson.getInt(3);
        int rows = locationJson.getInt(5);
        double minX = locationJson.getDouble(7);
        double maxX = locationJson.getDouble(9);
        double minY = locationJson.getDouble(11);
        double maxY = locationJson.getDouble(13);
        JSONArray counts = locationJson.getJSONArray(15);

        for (int row = 0; row < counts.length(); row++) {
            if (counts.isNull(row)) continue;
            JSONArray rowCounts = counts.getJSONArray(row);

            // There are items for this row
            for (int col = 0; col < rowCounts.length(); col++) {
                int placeCount = rowCounts.getInt(col);
                if (placeCount == 0) continue; // No place in this ROWxCOL

                Util.LatLng clusterPos = calculateClusterPosition(columns,
                                                                  rows,
                                                                  new Util.Rect(minX, minY, maxX, maxY),
                                                                  col,
                                                                  row);
                result.add(new PlaceCluster(placeCount, clusterPos.lat, clusterPos.lng));
            }
        }

        LOGGER.debug("Cluster | parsed " + result.size() + " clusters");
        return result;
    }

    public Util.LatLng calculateClusterPosition(int columns, int rows, Util.Rect boundingRect, int colIdx, int rowIdx) {
        double lat = boundingRect.maxY
                - rowIdx * (boundingRect.maxY - boundingRect.minY) / rows
                + (boundingRect.maxY - boundingRect.minY) / rows / 2;

        double lng = boundingRect.minX
                + colIdx * (boundingRect.maxX - boundingRect.minX) / colIdx
                + (boundingRect.maxX - boundingRect.minX) / columns / 2;

        return new Util.LatLng(lat, lng);
    }


}
