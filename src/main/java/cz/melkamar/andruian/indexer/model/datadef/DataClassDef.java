package cz.melkamar.andruian.indexer.model.datadef;

import java.util.ArrayList;
import java.util.List;

public class DataClassDef extends ClassDef {
    private final PropertyPath pathToLocationClass;
    private final List<SelectProperty> selectProperties;

    public DataClassDef(String sparqlEndpoint,
                        String classUri,
                        PropertyPath pathToLocationClass) {
        super(sparqlEndpoint, classUri);
        this.pathToLocationClass = pathToLocationClass;
        selectProperties = new ArrayList<>();
    }

    public PropertyPath getPathToLocationClass() {
        return pathToLocationClass;
    }

    public List<SelectProperty> getSelectProperties() {
        return selectProperties;
    }

    @Override
    public String toString() {
        return "DataClassDef{" +
                "pathToLocationClass=" + pathToLocationClass +
                ", selectProperties=" + selectProperties +
                ", sparqlEndpoint='" + sparqlEndpoint + '\'' +
                ", classUri='" + classUri + '\'' +
                '}';
    }
}
