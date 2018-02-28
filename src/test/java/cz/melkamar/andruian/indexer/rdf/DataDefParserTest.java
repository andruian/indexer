package cz.melkamar.andruian.indexer.rdf;

import cz.melkamar.andruian.indexer.model.datadef.DataClassDef;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.model.datadef.SelectProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.assertj.core.internal.Uris;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.List;

import static org.junit.Assert.*;

public class DataDefParserTest {
    Model datadefModel;

    @Before
    public void initRdfModel() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("rdf/datadef.ttl").getFile());
        datadefModel = ModelFactory.createDefaultModel();
        datadefModel.read(new FileInputStream(file), null, "TURTLE");
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
        Resource dataClassDefResource = datadefModel.getResource(URIs.Prefix.BLANK + "dataClassDef");
        DataClassDef dataClassDef = dataDefParser.parseDataClassDef(dataClassDefResource);

        assertEquals("<http://example.org/linksTo>/<http://example.org/linksTo2>/<http://example.org/linksTo3>",
                     dataClassDef.getPathToLocationClass().toString());
    }

    @Test
    public void parseSelectProperties() {
        DataDefParser dataDefParser = new DataDefParser(datadefModel);
        Resource dataClassDefResource = datadefModel.getResource(URIs.Prefix.BLANK + "dataClassDef");
        DataClassDef dataClassDef = dataDefParser.parseDataClassDef(dataClassDefResource);

        SelectProperty[] selectProperties = dataClassDef.getSelectProperties();
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
}