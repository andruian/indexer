package cz.melkamar.andruian.indexer.model.place;

/**
 * A class representing a cluster of Places generated server-side.
 */
public class PlaceCluster {
    private final int placesCount;
    private final String sourceDatadef;
    private final double latPos;
    private final double longPos;

    public PlaceCluster(int placesCount, String sourceDatadef, double latPos, double longPos) {
        this.placesCount = placesCount;
        this.sourceDatadef = sourceDatadef;
        this.latPos = latPos;
        this.longPos = longPos;
    }
}
