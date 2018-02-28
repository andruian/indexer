package cz.melkamar.andruian.indexer.rdf.sparql;

import cz.melkamar.andruian.indexer.Util;
import cz.melkamar.andruian.indexer.model.datadef.PropertyPath;
import cz.melkamar.andruian.indexer.model.datadef.SelectProperty;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IndexQueryBuilderTest {
    @Test
    public void build() throws Exception {
        IndexQueryBuilder builder = new IndexQueryBuilder(
                "[dataclassuri]",
                new PropertyPath("a","b","c"),
                "[locsparql]",
                new PropertyPath("path","to","lat"),
                new PropertyPath("path","to","long")
        );

        builder.addSelectProperty(new SelectProperty("selectA", new PropertyPath("sA", "x")));
        builder.addSelectProperty(new SelectProperty("selectB", new PropertyPath("sB", "y")));

        builder.excludeUri("[excludeA]");
        builder.excludeUri("[excludeB]");

        String query = builder.build();
        String queryExpected = Util.readResourceFileToString("expected/indexquery.sparql");
        System.out.println(query);
        assertEquals(queryExpected.replace("\r",""), query.replace("\r",""));
    }

}