package cz.melkamar.andruian.indexer.model.place;

/**
 * A class representing a cluster of Places generated server-side.
 *
 * There is no source datadef because Solr heatmap does not return them.
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
