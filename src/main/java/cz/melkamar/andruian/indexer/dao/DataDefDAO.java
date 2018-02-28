package cz.melkamar.andruian.indexer.dao;

import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.model.datadef.DataClassDef;
import cz.melkamar.andruian.indexer.model.datadef.DataDef;
import cz.melkamar.andruian.indexer.model.datadef.LocationDef;
import cz.melkamar.andruian.indexer.model.datadef.PropertyPath;
import cz.melkamar.andruian.indexer.rdf.DataDefParser;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.apache.jena.util.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Component
public class DataDefDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataDefDAO.class);
    static String andrPrefix = "";


    public static void main(String[] args) throws FileNotFoundException {
        String inputFileName = "d:\\cvut-checkouted\\andruian\\examples\\datadef.ttl";

        Model model = ModelFactory.createDefaultModel();
        InputStream in = FileManager.get().open(inputFileName);
        if (in == null) throw new IllegalArgumentException("File: " + inputFileName + " not found");
        model.read(in, null, "TURTLE");
        model.write(System.out, "TURTLE");

        DataDefParser dataDefParser = new DataDefParser(model);
        System.out.println(dataDefParser.parse());

    }

    public DataDef getDataDef(String uri) {
        throw new NotImplementedException();
    }
}
