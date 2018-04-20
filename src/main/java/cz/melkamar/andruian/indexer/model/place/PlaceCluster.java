package cz.melkamar.andruian.indexer.model.place;

/**
 * A class representing a cluster of Places generated server-side.
 *
 * There is no source datadef attached to it because Solr heatmap does not provide them.
 * If it is necessary to know it, it is up to the consumer to pair the response with the data definition
 * that was used when creating the query.
 */
public class PlaceCluster {
    private final int placesCount;
    private final double latPos;
    private final double longPos;

    public PlaceCluster(int placesCount, double latPos, double longPos) {
        this.placesCount = placesCount;
        this.latPos = latPos;
        this.longPos = longPos;
    }

    public int getPlacesCount() {
        return placesCount;
    }

    public double getLatPos() {
        return latPos;
    }

    public double getLongPos() {
        return longPos;
    }
}
