package cz.melkamar.andruian.indexer.exception;

public class QueryFormatException extends Exception {
    public QueryFormatException(String message) {
        super(message);
    }

    public QueryFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
