package cz.melkamar.andruian.indexer.exception;

public class DataDefIndexException extends RuntimeException {
    public DataDefIndexException() {
    }

    public DataDefIndexException(String message) {
        super(message);
    }

    public DataDefIndexException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataDefIndexException(Throwable cause) {
        super(cause);
    }
}
