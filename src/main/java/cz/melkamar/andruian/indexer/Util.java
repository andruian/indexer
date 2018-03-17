package cz.melkamar.andruian.indexer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;

public class Util {
    public static String readResourceFileToString(String resourcePath) throws IOException {
        ClassLoader classLoader = Util.class.getClassLoader();
        return IOUtils.toString(classLoader.getResourceAsStream(resourcePath), "utf-8");
    }

    public static String readStringFromResource(String resourcePath, Class clazz) throws IOException {
        ClassLoader classLoader = clazz.getClassLoader();
        File file = new File(classLoader.getResource(resourcePath).getFile());

        return FileUtils.readFileToString(file, "utf-8");
    }
}
