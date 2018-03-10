package cz.melkamar.andruian.indexer.rdf;

import cz.melkamar.andruian.indexer.exception.DataDefFormatException;
import cz.melkamar.andruian.indexer.exception.NotImplementedException;
import cz.melkamar.andruian.indexer.exception.RdfFormatException;
import cz.melkamar.andruian.indexer.model.datadef.*;
import cz.melkamar.andruian.indexer.net.DataDefFetcher;
import cz.melkamar.andruian.indexer.net.RdfFetcher;
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
    private final RdfFetcher rdfFetcher;

    public DataDefParser(Model model, RdfFetcher rdfFetcher) {
        this.model = model;
        this.rdfFetcher = rdfFetcher;
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
     * Process all andr:inludeRdf properties linked from the given resource.
     * Add data from the files to the current model.
     *
     * @param resource A resource to process.
     */
    public void processIncludeRdfs(Resource resource) {
        StmtIterator includeRdfsIter = resource.listProperties(new PropertyImpl(URIs.ANDR.includeRdf));
        while (includeRdfsIter.hasNext()) {
            String includeRdfUri = includeRdfsIter.nextStatement().getResource().toString();
            LOGGER.debug("Including {} in the current model", includeRdfUri);
            
            try {
                Model addModel = this.rdfFetcher.getDataDefFromUri(includeRdfUri);
                this.model.add(addModel);
            } catch (RdfFormatException | DataDefFormatException e) {
                LOGGER.error("Could not include " + includeRdfUri + " in the model", e);
                e.printStackTrace();
            }
        }
    }

    /**
     * Parse a LocationDef resource.
     *
     * @param locationDef
     * @return
     */
    public LocationDef parseLocationDef(Resource locationDef) {
        processIncludeRdfs(locationDef);

        String sparqlEndpoint = locationDef.getProperty(new PropertyImpl(URIs.ANDR.sparqlEndpoint))
                .getResource()
                .toString();

        String locClass = locationDef.getProperty(new PropertyImpl(URIs.ANDR._class))
                .getResource()
                .toString();

        Map<String, ClassToLocPath> locPathsMap = new HashMap<>();

        // Parse direct links of andr:classToLocPath
        locPathsMap.putAll(collectClassToLocPathsFromObject(locationDef));

        // Parse indirect links of andr:locationClassPathsSource
        StmtIterator locationClassPathsSourcesIter = locationDef.listProperties(
                new PropertyImpl(URIs.ANDR.locationClassPathsSource));
        while (locationClassPathsSourcesIter.hasNext()) {
            Resource locationClassPathsSource = locationClassPathsSourcesIter.nextStatement().getResource();
            locPathsMap.putAll(collectClassToLocPathsFromObject(locationClassPathsSource));
            if (locPathsMap.isEmpty()) {
                LOGGER.warn("No properties of type {} found for resource {}. If the resource is located in an " +
                                    "external file, use the property {} to link to it.",
                            URIs.ANDR.classToLocPath,
                            locationClassPathsSource.toString(),
                            URIs.ANDR.includeRdf);
            }
        }

        return new LocationDef(sparqlEndpoint, locClass, locPathsMap);
    }

    /**
     * Given a Resource, collect all {@link ClassToLocPath} objects linked to the Resource via the
     * andr:classToLocPath property.
     *
     * @param resource A resource to collect. Will be an andr:LocationClassDef or an andr:LocationClassPathsSource.
     * @return Map of class URI to {@link ClassToLocPath}.
     */
    private Map<String, ClassToLocPath> collectClassToLocPathsFromObject(Resource resource) {
        StmtIterator iterator = resource.listProperties(new PropertyImpl(URIs.ANDR.classToLocPath));
        Map<String, ClassToLocPath> result = new HashMap<>();

        while (iterator.hasNext()) {
            Resource pathDef = iterator.nextStatement().getResource();
            String pdClass = pathDef.getPropertyResourceValue(new PropertyImpl(URIs.ANDR._class)).toString();
            PropertyPath latPath = parsePropertyPath(pathDef.getPropertyResourceValue(new PropertyImpl(URIs.ANDR.lat)));
            PropertyPath longPath = parsePropertyPath(pathDef.getPropertyResourceValue(new PropertyImpl(URIs.ANDR._long)));
            ClassToLocPath classToLocPath = new ClassToLocPath(latPath, longPath, pdClass);
            result.put(pdClass, classToLocPath);
        }
        return result;
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
