package cz.melkamar.andruian.indexer;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Util {
    public static Model readModelFromResource(String resourcePath, Class clazz) throws FileNotFoundException {
        ClassLoader classLoader = clazz.getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());
        Model model = ModelFactory.createDefaultModel();
        model.read(new FileInputStream(file), null, "TURTLE");
        return model;
    }

    public static String readStringFromResource(String resourcePath, Class clazz) throws IOException {
        ClassLoader classLoader = clazz.getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());

        return FileUtils.readFileToString(file, "utf-8");
    }
}
