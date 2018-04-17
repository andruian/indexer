package cz.melkamar.andruian.indexer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static class Rect {
        public final double minX;
        public final double minY;
        public final double maxX;
        public final double maxY;

        public Rect(double minX, double minY, double maxX, double maxY) {
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }

        @Override
        public String toString() {
            return "Rect{" +
                    "minX=" + minX +
                    ", minY=" + minY +
                    ", maxX=" + maxX +
                    ", maxY=" + maxY +
                    '}';
        }
    }

    public static class LatLng {
        public final double lat;
        public final double lng;

        public LatLng(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        @Override
        public String
        toString() {
            return "LatLng{" +
                    "lat=" + lat +
                    ", lng=" + lng +
                    '}';
        }
    }
}
