package cz.melkamar.andruian.indexer.exception;

/**
 * Exception thrown when a reserved name is used during SPARQL query template instantiation.
 */
public class ReservedNameUsedException extends RuntimeException {
    public ReservedNameUsedException() {
    }

    public ReservedNameUsedException(String message) {
        super(message);
    }
}
