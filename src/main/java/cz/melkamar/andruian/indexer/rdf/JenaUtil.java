package cz.melkamar.andruian.indexer.rdf;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// TODO write tests
public class JenaUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(JenaUtil.class);

    public static Model modelFromString(String rdf) {
        JenaUtil.LOGGER.debug("Model from string: {}", rdf);

        Model model = ModelFactory.createDefaultModel();
        try {
            model.read(IOUtils.toInputStream(rdf, "utf-8"),
                       null,
                       "TURTLE"); // TODO do not force syntax to be turtle - how to determine?
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return model;
    }
}
