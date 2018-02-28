package cz.melkamar.andruian.indexer.model.datadef;

import java.util.Arrays;

public class DataClassDef extends ClassDef {
    private final PropertyPath pathToLocationClass;
    private final SelectProperty[] selectProperties;

    public DataClassDef(String sparqlEndpoint,
                        String classUri,
                        PropertyPath pathToLocationClass,
                        SelectProperty[] selectProperties) {
        super(sparqlEndpoint, classUri);
        this.pathToLocationClass = pathToLocationClass;
        this.selectProperties = selectProperties;
    }

    public PropertyPath getPathToLocationClass() {
        return pathToLocationClass;
    }

    public SelectProperty[] getSelectProperties() {
        return selectProperties;
    }

    @Override
    public String toString() {
        return "DataClassDef{" +
                "pathToLocationClass=" + pathToLocationClass +
                ", selectProperties=" + Arrays.toString(selectProperties) +
                ", sparqlEndpoint='" + sparqlEndpoint + '\'' +
                ", classUri='" + classUri + '\'' +
                '}';
    }
}
