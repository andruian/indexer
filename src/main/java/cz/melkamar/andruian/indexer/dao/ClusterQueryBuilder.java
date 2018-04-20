package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.Util;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * A builder for Solr heatmap-faceted query.
 *
 * The query's parameters are identical to a regular query, but contain a few extra parameters. See the {@link #build()}
 * method for more information.
 */
public class ClusterQueryBuilder {
    private final String solrUrl;
    private final String collection;
    private String type = null;
    private Double lat = null;
    private Double lng = null;
    private Double radius = null; // km
    private double distErrPct = 0.50;

    /**
     * @param solrUrl    The URL of the solr instance, e.g. https://localhost:8983/solr.
     * @param collection The name of the collection containing indexer data, e.g. andruian.
     */
    public ClusterQueryBuilder(String solrUrl, String collection) {
        this.solrUrl = solrUrl;
        this.collection = collection;
    }

    /**
     * @param solrUrl    The URL of the solr instance, e.g. https://localhost:8983/solr.
     * @param collection The name of the collection containing indexer data, e.g. andruian.
     * @param distErrPct The facet.heatmap.distErrPct value of the query, see <a href="https://lucene.apache.org/solr/guide/6_6/spatial-search.html#SpatialSearch-HeatmapFaceting">Solr heatmap faceting</a>.
     */
    public ClusterQueryBuilder(String solrUrl, String collection, double distErrPct) {
        this.solrUrl = solrUrl;
        this.collection = collection;
        this.distErrPct = distErrPct;
    }

    /**
     * Set the type (RDF class) of objects to search for.
     *
     * @param type The IRI of the class to search for.
     */
    public ClusterQueryBuilder setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Set the location around which to search for objects.
     *
     * @param lat    The latitude of the area around which to search.
     * @param lng    The longitude of the area around which to search.
     * @param radius The radius of the area in which to search, in kilometers.
     * @return
     */
    public ClusterQueryBuilder setLocation(double lat, double lng, double radius) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        return this;
    }

    /**
     * Build a Solr heatmap query. See <a href="https://lucene.apache.org/solr/guide/6_6/spatial-search.html#SpatialSearch-HeatmapFaceting">Solr heatmap faceting</a>
     * for more details.
     *
     * The query may look like the following:
     * <pre>
     * http://localhost:8983/solr/andruian/select?
     * d=2000 &amp;
     * facet.heatmap.geom=[%2214.411%2050.0%22%20TO%20%2214.412%2050.01%22] &amp;
     * facet.heatmap.gridLevel=6 &amp;
     * facet.heatmap=location &amp;
     * facet=true &amp;
     * fq={!bbox%20sfield=location} &amp;
     * pt=50.052828,14.439898 &amp;
     * q=type:%22http://example.org/SourceObjectA%22
     * </pre>
     *
     * <b>NOTE:</b> The query is already URL-encoded. When using in a HTTP client, make sure it does not re-encode
     * it, because that would not work.
     *
     * @return An encoded URL - the heatmap GET query.
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
     * Calculate a bounding rectangle of a circle defined by the given latitude, longitude and radius.
     *
     * An approximate algorithm for converting kilometers into GPS "units" taken from here
     * https://stackoverflow.com/questions/1253499/simple-calculations-for-working-with-lat-lon-km-distance
     *
     * @param lat    Latitude of the center of the circle.
     * @param lng    Longitude of the center of the circle.
     * @param radius The radius of the circle in kilometers.
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
