package cz.melkamar.andruian.indexer.model.datadef;

public abstract class ClassDef {
    protected final String sparqlEndpoint;
    protected final String classUri;

    public ClassDef(String sparqlEndpoint, String classUri) {
        this.sparqlEndpoint = sparqlEndpoint;
        this.classUri = classUri;
    }

    public String getSparqlEndpoint() {
        return sparqlEndpoint;
    }

    public String getClassUri() {
        return classUri;
    }
}
