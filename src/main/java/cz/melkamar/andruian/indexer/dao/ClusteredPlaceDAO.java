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

/**
 * A Data Access Object for retrieving clusters of places from the index database.
 *
 * The clustering functionality is implemented by the Solr database as heatmaps. The geospatial size
 * of a cluster depends on the size of the area the clusters are formed from. The number of cells the
 * given area is divided into should be roughly the same, regardless of the zoom level.
 *
 * If no area is provided, it defaults to encompass the whole world.
 */
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

    /**
     * Get all places indexed in the system as clusters.
     *
     * @return A list of {@link PlaceCluster} representing the matching data.
     */
    public List<PlaceCluster> getAllPlaces() {
        LOGGER.debug("Cluster | Fetching all places");
        List<PlaceCluster> result = new ArrayList<>();
        String query = new ClusterQueryBuilder(configuration.getDbSolrUri(), configuration.getDbSolrCollection(), configuration.getClusteringDistErrPct())
                .build();
        LOGGER.debug("Cluster | Using query: " + query);

        String response = netHelper.httpGetFromEncodedUri(query);
        return parseClusterQueryResponse(response);
    }

    /**
     * Get all places of the given RDF class indexed in the system as clusters.
     *
     * @param classUri The IRI of a RDF class to search for.
     * @return A list of {@link PlaceCluster} representing the matching data.
     */
    public List<PlaceCluster> getPlacesOfClass(String classUri) {
        LOGGER.debug("Cluster | Fetching places of type " + classUri);
        List<PlaceCluster> result = new ArrayList<>();
        String query = new ClusterQueryBuilder(configuration.getDbSolrUri(), configuration.getDbSolrCollection(), configuration.getClusteringDistErrPct())
                .setType(classUri)
                .build();
        LOGGER.debug("Cluster | Using query: " + query);

        String response = netHelper.httpGetFromEncodedUri(query);
        return parseClusterQueryResponse(response);
    }

    /**
     * Get places inside an area defined by the coordinates as clusters.
     *
     * @param latCoord  The latitude of the center of the area to search in.
     * @param longCoord The longitude of the center of the area to search in.
     * @param radius    The radius from the center of the area to search in, in kilometers.
     * @return A list of {@link PlaceCluster} representing the matching data.
     */
    public List<PlaceCluster> getPlacesAroundPoint(double latCoord, double longCoord, double radius) {
        LOGGER.debug("Cluster | getPlacesAroundPoint {} {} {}", latCoord, longCoord, radius);
        List<PlaceCluster> result = new ArrayList<>();
        String query = new ClusterQueryBuilder(configuration.getDbSolrUri(), configuration.getDbSolrCollection(), configuration.getClusteringDistErrPct())
                .setLocation(latCoord, longCoord, radius)
                .build();
        LOGGER.debug("Cluster | Using query: " + query);

        String response = netHelper.httpGetFromEncodedUri(query);
        return parseClusterQueryResponse(response);
    }

    /**
     * Get places inside an area defined by the coordinates, of a particular RDF class, as clusters.
     *
     * @param classUri  The IRI of a RDF class to search for.
     * @param latCoord  The latitude of the center of the area to search in.
     * @param longCoord The longitude of the center of the area to search in.
     * @param radius    The radius from the center of the area to search in, in kilometers.
     * @return A list of {@link PlaceCluster} representing the matching data.
     */
    public List<PlaceCluster> getPlacesAroundPointOfClass(String classUri,
                                                          double latCoord,
                                                          double longCoord,
                                                          double radius) {
        LOGGER.debug("Cluster | getPlacesAroundPointOfClass {} {} {} {}", classUri, latCoord, longCoord, radius);
        List<PlaceCluster> result = new ArrayList<>();
        String query = new ClusterQueryBuilder(configuration.getDbSolrUri(), configuration.getDbSolrCollection(), configuration.getClusteringDistErrPct())
                .setLocation(latCoord, longCoord, radius)
                .setType(classUri)
                .build();
        LOGGER.debug("Cluster | Using query: " + query);

        String response = netHelper.httpGetFromEncodedUri(query);
        return parseClusterQueryResponse(response);
    }

    /**
     * Parse the response of a cluster/heatmap query from Solr. An example of the query format is provided
     * in the test class ClusteredPlaceDAOTest.
     *
     * The response does not directly return GPS locations of the clusters. Instead, it divides the rectangle
     * that is being searched into an orthogonal grid and returns the number of columns and rows of such grid.
     * The grid of clusters is then represented as a 2D array of numbers, each number specifying how many clusters
     * belong to each of the grid cells. The method {@link ClusteredPlaceDAO#calculateClusterPosition(int, int, Util.Rect, int, int)}
     * is used to calculate the GPS position of the center of each grid cell (and effectively the approximate location
     * of the place cluster it represents).
     *
     * @param response The query JSON response.
     * @return A parsed list of {@link PlaceCluster} objects.
     */
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

        if (locationJson.isNull(15)){
            return result;
        }
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

    /**
     * Calculates the position of the center of a grid cell, given the grid bounding rectangle coordinates, the
     * number of rows and columns and the row/col index of the cell. For more explanation why, refer to the
     * {@link #parseClusterQueryResponse(String)} method.
     *
     * The algorithm is straightforward - split the rectangle evenly to form a grid, find the coordinates of the edge
     * of the cell we are looking for and add half of the cell's width/height to find its center.
     *
     * @param columns      The number of columns the grid contains.
     * @param rows         The number of rows the grid contains.
     * @param boundingRect The bounding rectangle of the whole grid. The rectangle is orthogonal and defined by the
     *                     bottom-leftmost and top-rightmost points.
     * @param colIdx       The column index of the cell in the grid for which to calculate the position. 0-indexed.
     * @param rowIdx       The row index of the cell in the grid for which to calculate the position. 0-indexed.
     * @return The latitude and longitude of the center of the given grid cell.
     */
    public Util.LatLng calculateClusterPosition(int columns, int rows, Util.Rect boundingRect, int colIdx, int rowIdx) {
        double lat = boundingRect.maxY
                - rowIdx * (boundingRect.maxY - boundingRect.minY) / rows
                - (boundingRect.maxY - boundingRect.minY) / rows / 2;

        double lng = boundingRect.minX
                + colIdx * (boundingRect.maxX - boundingRect.minX) / columns
                + (boundingRect.maxX - boundingRect.minX) / columns / 2;

        LOGGER.trace("clusterPos: " + colIdx + "," + rowIdx + " (" + boundingRect + ") cols:" + columns + " rows:" + rows + " -> " + new Util.LatLng(
                lat,
                lng));
        return new Util.LatLng(lat, lng);
    }


}
