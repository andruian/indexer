package cz.melkamar.andruian.indexer.exception;

public class SparqlQueryException  extends Exception {
    public SparqlQueryException(String message) {
        super(message);
    }

    public SparqlQueryException(Throwable cause) {
        super(cause);
    }

    public SparqlQueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
