package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.model.place.PlaceCluster;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class ClusteredPlaceDAOTest {

    @Test
    public void parseClusterQueryResponse() {
        ClusteredPlaceDAO dao = new ClusteredPlaceDAO(null, null);
        List<PlaceCluster> placeClusters = dao.parseClusterQueryResponse(clusterResponse());

        assertEquals(4, placeClusters.size());
    }

    private String clusterResponse(){
        return "{\n" +
                "  \"responseHeader\": {\n" +
                "    \"status\": 0,\n" +
                "    \"QTime\": 48,\n" +
                "    \"params\": {\n" +
                "      \"q\": \"*:*\",\n" +
                "      \"facet.heatmap\": \"location\",\n" +
                "      \"facet.heatmap.geom\": \"[\\\"14.411 50.0\\\" TO \\\"14.412 50.01\\\"]\",\n" +
                "      \"facet\": \"true\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"response\": {\n" +
                "    \"numFound\": 6658,\n" +
                "    \"start\": 0,\n" +
                "    \"docs\": [\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21931739\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"50.009254,14.411614\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21931739\",\n" +
                "        \"label\": \"Andělova 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"Andělova\",\n" +
                "        \"PSC_prop_dynstr\": \"14300\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642759991296\n" +
                "      },\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21915181\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"50.004985,14.403967\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21915181\",\n" +
                "        \"label\": \"Obchodní náměstí 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"Obchodní náměstí\",\n" +
                "        \"PSC_prop_dynstr\": \"14300\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642892111872\n" +
                "      },\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21948402\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"50.056284,14.441616\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21948402\",\n" +
                "        \"label\": \"U Libušiných lázní 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"U Libušiných lázní\",\n" +
                "        \"PSC_prop_dynstr\": \"14000\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642894209024\n" +
                "      },\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21909423\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"50.058922,14.45133\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21909423\",\n" +
                "        \"label\": \"Adamovská 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"Adamovská\",\n" +
                "        \"PSC_prop_dynstr\": \"14000\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642895257600\n" +
                "      },\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21934185\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"50.000426,14.416753\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21934185\",\n" +
                "        \"label\": \"U čekárny 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"U čekárny\",\n" +
                "        \"PSC_prop_dynstr\": \"14300\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642896306176\n" +
                "      },\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21955565\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"49.992431,14.468459\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21955565\",\n" +
                "        \"label\": \"U Jednoty 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"U Jednoty\",\n" +
                "        \"PSC_prop_dynstr\": \"14200\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642896306177\n" +
                "      },\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21926166\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"49.997262,14.423355\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21926166\",\n" +
                "        \"label\": \"Odbočná 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"Odbočná\",\n" +
                "        \"PSC_prop_dynstr\": \"14300\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642897354752\n" +
                "      },\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21905479\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"50.055191,14.457391\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21905479\",\n" +
                "        \"label\": \"U Hellady 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"U Hellady\",\n" +
                "        \"PSC_prop_dynstr\": \"14000\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642898403328\n" +
                "      },\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21932646\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"50.005396,14.40382\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21932646\",\n" +
                "        \"label\": \"U zastávky 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"U zastávky\",\n" +
                "        \"PSC_prop_dynstr\": \"14300\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642899451904\n" +
                "      },\n" +
                "      {\n" +
                "        \"iri\": \"http://src.com/aeiou/https%3A%2F%2Fruian.linked.opendata.cz%2Fzdroj%2Fadresní-místa%2F21950288\",\n" +
                "        \"type\": \"http://example.org/SourceObjectAEIOU\",\n" +
                "        \"location\": \"50.059792,14.431718\",\n" +
                "        \"locationObjectIri\": \"https://ruian.linked.opendata.cz/zdroj/adresní-místa/21950288\",\n" +
                "        \"label\": \"U gymnázia 1\",\n" +
                "        \"srcddf_dynstr\": \"http://foo/dataDefVowel\",\n" +
                "        \"StreetName_prop_dynstr\": \"U gymnázia\",\n" +
                "        \"PSC_prop_dynstr\": \"14000\",\n" +
                "        \"StreetNum_prop_dynstr\": \"1\",\n" +
                "        \"_version_\": 1597934642900500480\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  \"facet_counts\": {\n" +
                "    \"facet_queries\": {},\n" +
                "    \"facet_fields\": {},\n" +
                "    \"facet_ranges\": {},\n" +
                "    \"facet_intervals\": {},\n" +
                "    \"facet_heatmaps\": {\n" +
                "      \"location\": [\n" +
                "        \"gridLevel\",\n" +
                "        8,\n" +
                "        \"columns\",\n" +
                "        3,\n" +
                "        \"rows\",\n" +
                "        59,\n" +
                "        \"minX\",\n" +
                "        14.410972595214844,\n" +
                "        \"maxX\",\n" +
                "        14.412002563476562,\n" +
                "        \"minY\",\n" +
                "        49.99998092651367,\n" +
                "        \"maxY\",\n" +
                "        50.010108947753906,\n" +
                "        \"counts_ints2D\",\n" +
                "        [\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          [\n" +
                "            0,\n" +
                "            0,\n" +
                "            1\n" +
                "          ],\n" +
                "          [\n" +
                "            0,\n" +
                "            1,\n" +
                "            1\n" +
                "          ],\n" +
                "          null,\n" +
                "          [\n" +
                "            0,\n" +
                "            0,\n" +
                "            1\n" +
                "          ],\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null,\n" +
                "          null\n" +
                "        ]\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}