package cz.melkamar.andruian.indexer.rdf;

import cz.melkamar.andruian.indexer.dao.DataDefDAO;
import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.model.datadef.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.rdf.model.impl.ResourceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DataDefParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataDefDAO.class);

    private final Model model;

    public DataDefParser(Model model) {
        this.model = model;
    }

    public DataDef parse() {
        String dataDefClassURI = ANDR.DataDef;

        ResIterator iter = model.listSubjectsWithProperty(new PropertyImpl(RDF.type),
                                                          new ResourceImpl(dataDefClassURI));
        while (iter.hasNext()) {
            Resource dataDefResource = iter.next();
            LocationDef locationDef = parseLocationDef(dataDefResource);
            DataClassDef dataClassDef = parseDataClassDef(dataDefResource);
            DataDef dataDef = new DataDef(dataDefResource.getURI(), locationDef, dataClassDef);

            LOGGER.debug("dataDef:      {}", dataDef);
            return dataDef; // todo this should be better
        }
        System.out.println("done");
        return null;
    }

    /**
     * Create a POJO from JENA RDF Resource.
     *
     * @param dataDef
     * @return
     */
    private DataClassDef parseDataClassDef(Resource dataDef) {
        LOGGER.debug("parseDataClassDef: {}", dataDef);

        Resource dataClassDefResource = dataDef.getProperty(new PropertyImpl(ANDR.dataClassDef))
                .getResource();

        String sparqlEndpointURI = dataClassDefResource.getProperty(new PropertyImpl(ANDR.sparqlEndpoint))
                .getResource()
                .toString();

        String classURI = dataClassDefResource.getProperty(new PropertyImpl(ANDR._class))
                .getResource()
                .toString();

        SelectProperty[] selectProperties = parseSelectProperties(dataClassDefResource);

        PropertyPath propertyPath = parsePropertyPath(dataClassDefResource.getPropertyResourceValue(new PropertyImpl(
                ANDR.pathToLocationClass)));


        DataClassDef dataClassDef = new DataClassDef(sparqlEndpointURI, classURI, propertyPath, selectProperties);


        LOGGER.debug("sparqlUri:    {}", sparqlEndpointURI);
        LOGGER.debug("classUri:     {}", classURI);
        return dataClassDef;
    }

    /**
     * Parse all andr:selectProperty of a given dataClassDef JENA resource.
     *
     * @param dataClassDef
     * @return
     */
    private SelectProperty[] parseSelectProperties(Resource dataClassDef) {
        StmtIterator iterator = dataClassDef.listProperties(new PropertyImpl(ANDR.selectProperty));
        List<SelectProperty> properties = new ArrayList<>();
        while (iterator.hasNext()) {
            Resource selectPropertyResource = iterator.nextStatement().getResource();
            String name = selectPropertyResource.getProperty(new PropertyImpl(Prefix.s + "name")).getLiteral()
                    .toString();

            Resource pathResource = selectPropertyResource.getPropertyResourceValue(new PropertyImpl(SP.path));
            PropertyPath propertyPath = parsePropertyPath(pathResource);

            properties.add(new SelectProperty(name, propertyPath));
        }

        return properties.toArray(new SelectProperty[properties.size()]);
    }

    private LocationDef parseLocationDef(Resource dataDef) {
        LOGGER.warn("Not implemented yet!");
        return null;
    }

    private String getResourceType(Resource resource) {
        StmtIterator iterator = resource.listProperties();
        while (iterator.hasNext()) System.out.println(iterator.nextStatement());
        Statement statement = resource.getProperty(new PropertyImpl(URI_RDF_TYPE));
        if (statement == null) return null;
        return statement.getResource().toString();
    }

    /**
     * Parse a Path RDF Resource into a PropertyPath object.
     *
     * @param seqPath Resource of type sp:SeqPath.
     * @return
     */
    private PropertyPath parsePropertyPath(Resource seqPath) {
        String type = getResourceType(seqPath);
        if (!type.equals(SP.SeqPath)) {
            throw new NotImplementedException("PropertyPath type " + type + " not implemented yet.");
        }

        List<String> pathParts = new ArrayList<>();
        while (true) {
            String part1 = seqPath.getPropertyResourceValue(new PropertyImpl(SP.path1)).toString();
            pathParts.add(part1);

            Resource part2 = seqPath.getPropertyResourceValue(new PropertyImpl(SP.path2));
            if (part2 == null) { // If no part2, just ignore it
                break;
            } else {
                String part2type = getResourceType(part2);
                if (part2type == null) {
                    pathParts.add(part2.toString());
                    break;
                } else if (part2type.equals(SP.SeqPath)) {
                    seqPath = part2;
                } else {
                    throw new NotImplementedException("Unknown path2 type " + part2type);
                }
            }
        }

        return new PropertyPath(pathParts.toArray(new String[pathParts.size()]));
    }

    public final static String URI_RDF_TYPE = Prefix.rdf + "type";

    public static class Prefix {
        public final static String andr = "http://example.andruian.com/ontology/";
        public final static String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
        public final static String ruian = "http://ruian.linked.opendata.cz/ontology/";
        public final static String sp = "http://spinrdf.org/sp#";
        public final static String s = "http://schema.org/";
        public final static String ex = "http://example.org/";
        public final static String BLANK = "http://foo/";
    }

    public static class ANDR {
        public final static String dataClassDef = Prefix.andr + "dataClassDef";
        public final static String sparqlEndpoint = Prefix.andr + "sparqlEndpoint";
        public final static String _class = Prefix.andr + "class";
        public final static String pathToLocationClass = Prefix.andr + "pathToLocationClass";
        public final static String selectProperty = Prefix.andr + "selectProperty";
        public final static String DataClassDef = Prefix.andr + "DataClassDef";
        public final static String DataDef = Prefix.andr + "DataDef";
    }

    public static class SP {
        public final static String path = Prefix.sp + "path";
        public final static String path1 = Prefix.sp + "path1";
        public final static String path2 = Prefix.sp + "path2";
        public final static String SeqPath = Prefix.sp + "SeqPath";
    }

    public static class RDF {
        public final static String type = Prefix.rdf + "type";
    }
}
