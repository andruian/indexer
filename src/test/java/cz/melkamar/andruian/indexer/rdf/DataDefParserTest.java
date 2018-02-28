package cz.melkamar.andruian.indexer.rdf;

import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.junit.Before;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

public class DataDefParserTest {
    Model datadefModel;

    @Before
    public void initRdfModel() throws FileNotFoundException {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("rdf/datadef.ttl").getFile());
        datadefModel = ModelFactory.createDefaultModel();
        datadefModel.read(new FileInputStream(file), null,"TURTLE");
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
        DataDef dataDef = dataDefParser.parse();

        assertEquals("<http://example.org/linksTo>/<http://example.org/linksTo2>/<http://example.org/linksTo3>",
                     dataDef.getDataClassDef().getPathToLocationClass().toString());
    }
}