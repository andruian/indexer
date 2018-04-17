package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.Util;
import org.springframework.web.util.UriComponentsBuilder;

public class ClusterQueryBuilder {
    private final String solrUrl;
    private final String collection;
    private String type = null;
    private Double lat = null;
    private Double lng = null;
    private Double radius = null; // km
    private double distErrPct = 0.50;

    /**
     * @param solrUrl
     * @param collection
     */
    public ClusterQueryBuilder(String solrUrl, String collection) {
        this.solrUrl = solrUrl;
        this.collection = collection;
    }

    /**
     * @param solrUrl
     * @param collection
     */
    public ClusterQueryBuilder(String solrUrl, String collection, double distErrPct) {
        this.solrUrl = solrUrl;
        this.collection = collection;
        this.distErrPct = distErrPct;
    }

    public ClusterQueryBuilder setType(String type) {
        this.type = type;
        return this;
    }

    public ClusterQueryBuilder setLocation(double lat, double lng, double radius) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        return this;
    }

    /**
     * http://localhost:8983/solr/andruian/select?
     * d=2000 &
     * facet.heatmap.geom=[%2214.411%2050.0%22%20TO%20%2214.412%2050.01%22] &
     * facet.heatmap.gridLevel=6 &
     * facet.heatmap=location &
     * facet=true &
     * fq={!bbox%20sfield=location} &
     * pt=50.052828,14.439898 &
     * q=type:%22http://example.org/SourceObjectA%22
     */
    public String build() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(solrUrl);
        builder.path("/" + collection);
        builder.path("/select");

        builder.queryParam("facet.heatmap", "location");
        builder.queryParam("facet", "true");
        builder.queryParam("facet.heatmap.distErrPct", String.valueOf(distErrPct));

        // Spatial-bounded
        if (lat != null) {
            builder.queryParam("fq", "{!bbox sfield=location}");
            builder.queryParam("pt", lat + "," + lng);
            builder.queryParam("d", radius);

            Util.Rect boundRect = calculateBoundingRect(lat, lng, radius);
            builder.queryParam("facet.heatmap.geom",
                               "[\"" + boundRect.minX + " " + boundRect.minY + "\" " +
                                       "TO " +
                                       "\"" + boundRect.maxX + " " + boundRect.maxY + "\"]");
        }

        if (type != null) builder.queryParam("q", "type:\"" + type + "\"");
        else builder.queryParam("q", "*:*");

        return builder.build().encode().toString();
    }

    /**
     * Approximate algorithm taken from here
     * https://stackoverflow.com/questions/1253499/simple-calculations-for-working-with-lat-lon-km-distance
     *
     * @param lat
     * @param lng
     * @param radius kilometers
     */
    private Util.Rect calculateBoundingRect(double lat, double lng, double radius) {
        double latDelta = radius / 110.574;
        double lngDelta = radius / (111.320 * Math.cos(Math.toRadians(lat)));

        return new Util.Rect(
                lng - lngDelta, lat - latDelta,
                lng + lngDelta, lat + latDelta
        );
    }

}
