package cz.melkamar.andruian.indexer.rdf;

import cz.melkamar.andruian.indexer.exception.DataDefFormatException;
import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.model.datadef.*;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(DataDefFetcher.class);

    private final Model model;

    public DataDefParser(Model model) {
        this.model = model;
    }

    public DataDef parse() throws DataDefFormatException {
        String dataDefClassURI = URIs.ANDR.DataDef;

        ResIterator iter = model.listSubjectsWithProperty(new PropertyImpl(URIs.RDF.type),
                                                          new ResourceImpl(dataDefClassURI));
        while (iter.hasNext()) {
            Resource dataDefResource = iter.next();

            Resource locationClassDefResource = dataDefResource.getPropertyResourceValue(new PropertyImpl(URIs.ANDR.locationDef));
            LocationDef locationDef = parseLocationDef(locationClassDefResource);

            Resource sourceClassDefResource = dataDefResource.getPropertyResourceValue(new PropertyImpl(URIs.ANDR.sourceClassDef));
            SourceClassDef sourceClassDef = parseSourceClassDef(sourceClassDefResource);

            DataDef dataDef = new DataDef(dataDefResource.getURI(), locationDef, sourceClassDef);
            LOGGER.debug("Parsed dataDef: {}", dataDef);
            return dataDef; // todo this should be better
        }

        throw new DataDefFormatException("No property of type " + dataDefClassURI + " found in RDF");
    }

    /**
     * Create a POJO from JENA RDF Resource.
     *
     * @param sourceClassDefResource
     * @return
     */
    public SourceClassDef parseSourceClassDef(Resource sourceClassDefResource) {
        String sparqlEndpointURI = sourceClassDefResource.getProperty(new PropertyImpl(URIs.ANDR.sparqlEndpoint))
                .getResource()
                .toString();

        String classURI = sourceClassDefResource.getProperty(new PropertyImpl(URIs.ANDR._class))
                .getResource()
                .toString();

        SelectProperty[] selectProperties = parseSelectProperties(sourceClassDefResource);

        PropertyPath propertyPath = parsePropertyPath(sourceClassDefResource.getPropertyResourceValue(new PropertyImpl(
                URIs.ANDR.pathToLocationClass)));


        SourceClassDef sourceClassDef = new SourceClassDef(sparqlEndpointURI, classURI, propertyPath, selectProperties);
        return sourceClassDef;
    }

    /**
     * Parse all andr:selectProperty of a given sourceClassDef JENA resource.
     *
     * @param sourceClassDef
     * @return
     */
    public SelectProperty[] parseSelectProperties(Resource sourceClassDef) {
        StmtIterator iterator = sourceClassDef.listProperties(new PropertyImpl(URIs.ANDR.selectProperty));
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
        // TODO schema format changed! there is not LocationClassPathsSource class inbetween LocationDef and ClassToLocPath
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
        if (type == null) {
            // No type specified for this node - check if there is a path1 and if so, just assume user forgot to fill
            // in sp:SeqPath type.
            if (seqPath.getProperty(new PropertyImpl(URIs.SP.path1)) == null) {
                throw new NotImplementedException(
                        "No type specified and path1 property not found for an object that should be a sp:SeqPath.");
            }
        } else if (!type.equals(URIs.SP.SeqPath)) {
            // Do not force users to have seqpath type - check if maybe the 
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
                    // Check if part2 has path1 in its properties. If yes, assume it's a node with more path elements
                    if (part2.getProperty(new PropertyImpl(URIs.SP.path1)) == null) {
                        pathParts.add(part2.toString());
                        break;
                    } else {
                        seqPath = part2;
                    }
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
