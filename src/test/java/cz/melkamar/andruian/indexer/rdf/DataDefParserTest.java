package cz.melkamar.andruian.indexer.rdf;

import cz.melkamar.andruian.indexer.Util;
import cz.melkamar.andruian.indexer.model.datadef.ClassToCoordPropPath;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.model.datadef.SelectProperty;
import cz.melkamar.andruian.indexer.model.datadef.SourceClassDef;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DataDefParserTest {
    Model datadefModel;

    @Before
    public void initRdfModel() throws FileNotFoundException {
        datadefModel = Util.readModelFromResource("rdf/test-parse-datadef.ttl", this.getClass());
    }

    @Test
    public void parseDoesNotThrowException() {
        DataDefParser dataDefParser = new DataDefParser(datadefModel);
        dataDefParser.parse();
    }

    @Test
    public void parseDataDefUri() {
        DataDefParser dataDefParser = new DataDefParser(datadefModel);
        DataDef dataDef = dataDefParser.parse();

        assertEquals("http://foo/dataDef", dataDef.getUri());
    }

    @Test
    public void parsePathToLocationClass() {
        DataDefParser dataDefParser = new DataDefParser(datadefModel);
        Resource sourceClassDefResource = datadefModel.getResource(URIs.Prefix.BLANK + "sourceClassDef");
        SourceClassDef sourceClassDef = dataDefParser.parseSourceClassDef(sourceClassDefResource);

        assertEquals("<http://example.org/linksTo>/<http://example.org/linksTo2>/<http://example.org/linksTo3>",
                     sourceClassDef.getPathToLocationClass().toString());
    }

    @Test
    public void parseSelectProperties() {
        DataDefParser dataDefParser = new DataDefParser(datadefModel);
        Resource sourceClassDefResource = datadefModel.getResource(URIs.Prefix.BLANK + "sourceClassDef");
        SourceClassDef sourceClassDef = dataDefParser.parseSourceClassDef(sourceClassDefResource);

        SelectProperty[] selectProperties = sourceClassDef.getSelectProperties();
        assertEquals(2, selectProperties.length);

        for (SelectProperty selectProperty : selectProperties) {
            assertTrue(selectProperty.getName().equals("foobar") || selectProperty.getName().equals("another"));
            if (selectProperty.getName().equals("foobar")) {
                assertEquals(1, selectProperty.getPath().getPathElements().length);
                assertEquals(URIs.Prefix.ex + "id", selectProperty.getPath().getPathElements()[0]);
            } else {
                assertEquals(2, selectProperty.getPath().getPathElements().length);
                assertEquals(URIs.Prefix.ex + "abc", selectProperty.getPath().getPathElements()[0]);
                assertEquals(URIs.Prefix.ex + "def", selectProperty.getPath().getPathElements()[1]);
            }
        }
    }

    @Test
    public void parseLocationDef() {
        DataDefParser dataDefParser = new DataDefParser(datadefModel);
        DataDef dataDef = dataDefParser.parse();

        assertEquals("http://ruian.linked.opendata.cz/sparql", dataDef.getLocationDef().getSparqlEndpoint());
        assertEquals(URIs.Prefix.ruian + "AdresniMisto", dataDef.getLocationDef().getClassUri());
        assertEquals(1, dataDef.getLocationDef().getPathsToGps().size());
    }

    /**
     * Parse path from a Location Class to its GPS.
     */
    @Test
    public void parsePropertyPathLocClass() {
        DataDefParser dataDefParser = new DataDefParser(datadefModel);
        DataDef dataDef = dataDefParser.parse();

        ClassToCoordPropPath paths = dataDef.getLocationDef().getPathToGps(URIs.Prefix.ruian + "AdresniMisto");
        assertEquals(URIs.Prefix.ruian + "adresniBod", paths.getLatCoord().getPathElements()[0]);
        assertEquals(URIs.Prefix.s + "geo", paths.getLatCoord().getPathElements()[1]);
        assertEquals(URIs.Prefix.s + "latitude", paths.getLatCoord().getPathElements()[2]);

        assertEquals(URIs.Prefix.ruian + "adresniBod", paths.getLongCoord().getPathElements()[0]);
        assertEquals(URIs.Prefix.s + "geo", paths.getLongCoord().getPathElements()[1]);
        assertEquals(URIs.Prefix.s + "longitude", paths.getLongCoord().getPathElements()[2]);
    }
}