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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataDefParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataDefDAO.class);

    private final Model model;

    public DataDefParser(Model model) {
        this.model = model;
    }

    public DataDef parse() {
        String dataDefClassURI = URIs.ANDR.DataDef;

        ResIterator iter = model.listSubjectsWithProperty(new PropertyImpl(URIs.RDF.type),
                                                          new ResourceImpl(dataDefClassURI));
        while (iter.hasNext()) {
            Resource dataDefResource = iter.next();

            Resource locationClassDefResource = dataDefResource.getPropertyResourceValue(new PropertyImpl(URIs.ANDR.locationDef));
            LocationDef locationDef = parseLocationDef(locationClassDefResource);

            Resource dataClassDefResource = dataDefResource.getPropertyResourceValue(new PropertyImpl(URIs.ANDR.dataClassDef));
            DataClassDef dataClassDef = parseDataClassDef(dataClassDefResource);

            DataDef dataDef = new DataDef(dataDefResource.getURI(), locationDef, dataClassDef);
            LOGGER.debug("Parsed dataDef: {}", dataDef);
            return dataDef; // todo this should be better
        }
        return null;
    }

    /**
     * Create a POJO from JENA RDF Resource.
     *
     * @param dataClassDefResource
     * @return
     */
    public DataClassDef parseDataClassDef(Resource dataClassDefResource) {
        String sparqlEndpointURI = dataClassDefResource.getProperty(new PropertyImpl(URIs.ANDR.sparqlEndpoint))
                .getResource()
                .toString();

        String classURI = dataClassDefResource.getProperty(new PropertyImpl(URIs.ANDR._class))
                .getResource()
                .toString();

        SelectProperty[] selectProperties = parseSelectProperties(dataClassDefResource);

        PropertyPath propertyPath = parsePropertyPath(dataClassDefResource.getPropertyResourceValue(new PropertyImpl(
                URIs.ANDR.pathToLocationClass)));


        DataClassDef dataClassDef = new DataClassDef(sparqlEndpointURI, classURI, propertyPath, selectProperties);
        return dataClassDef;
    }

    /**
     * Parse all andr:selectProperty of a given dataClassDef JENA resource.
     *
     * @param dataClassDef
     * @return
     */
    public SelectProperty[] parseSelectProperties(Resource dataClassDef) {
        StmtIterator iterator = dataClassDef.listProperties(new PropertyImpl(URIs.ANDR.selectProperty));
        List<SelectProperty> properties = new ArrayList<>();
        while (iterator.hasNext()) {
            Resource selectPropertyResource = iterator.nextStatement().getResource();
            String name = selectPropertyResource.getProperty(new PropertyImpl(URIs.Prefix.s + "name")).getLiteral()
                    .toString();

            Resource pathResource = selectPropertyResource.getPropertyResourceValue(new PropertyImpl(URIs.SP.path));
            PropertyPath propertyPath = parsePropertyPath(pathResource);

            properties.add(new SelectProperty(name, propertyPath));
        }

        return properties.toArray(new SelectProperty[properties.size()]);
    }

    /**
     * Parse a LocationDef resource.
     *
     * @param locationDef
     * @return
     */
    public LocationDef parseLocationDef(Resource locationDef) {
        String sparqlEndpoint = locationDef.getProperty(new PropertyImpl(URIs.ANDR.sparqlEndpoint))
                .getResource()
                .toString();

        String locClass = locationDef.getProperty(new PropertyImpl(URIs.ANDR._class))
                .getResource()
                .toString();

        Map<String, ClassToCoordPropPath> locPathsMap = new HashMap<>();
        StmtIterator pathDefs = locationDef.listProperties(new PropertyImpl(URIs.ANDR.classToLocPath));
        while (pathDefs.hasNext()) {
            Resource pathDef = pathDefs.nextStatement().getResource();
            String pdClass = pathDef.getPropertyResourceValue(new PropertyImpl(URIs.ANDR._class)).toString();
            PropertyPath latPath = parsePropertyPath(pathDef.getPropertyResourceValue(new PropertyImpl(URIs.ANDR.lat)));
            PropertyPath longPath = parsePropertyPath(pathDef.getPropertyResourceValue(new PropertyImpl(URIs.ANDR._long)));
            ClassToCoordPropPath classToCoordPropPath = new ClassToCoordPropPath(latPath, longPath, pdClass);
            locPathsMap.put(pdClass, classToCoordPropPath);
        }
        return new LocationDef(sparqlEndpoint, locClass, locPathsMap);
    }

    public String getResourceType(Resource resource) {
        Statement statement = resource.getProperty(new PropertyImpl(URIs.RDF.type));
        if (statement == null) return null;
        return statement.getResource().toString();
    }

    /**
     * Parse a Path RDF Resource into a PropertyPath object.
     *
     * @param seqPath Resource of type sp:SeqPath.
     * @return
     */
    public PropertyPath parsePropertyPath(Resource seqPath) {
        String type = getResourceType(seqPath);
        if (!type.equals(URIs.SP.SeqPath)) {
            throw new NotImplementedException("PropertyPath type " + type + " not implemented yet.");
        }

        List<String> pathParts = new ArrayList<>();
        while (true) {
            String part1 = seqPath.getPropertyResourceValue(new PropertyImpl(URIs.SP.path1)).toString();
            pathParts.add(part1);

            Resource part2 = seqPath.getPropertyResourceValue(new PropertyImpl(URIs.SP.path2));
            if (part2 == null) { // If no part2, just ignore it
                break;
            } else {
                String part2type = getResourceType(part2);
                if (part2type == null) {
                    pathParts.add(part2.toString());
                    break;
                } else if (part2type.equals(URIs.SP.SeqPath)) {
                    seqPath = part2;
                } else {
                    throw new NotImplementedException("Unknown path2 type " + part2type);
                }
            }
        }

        return new PropertyPath(pathParts.toArray(new String[pathParts.size()]));
    }

}
