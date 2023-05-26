package hao.common.http.exception;

public class InternalException extends RuntimeException {
    private final int status;
    private final String catalog;

    public InternalException(int status, String message) {
        super(message);
        this.status = status;
        catalog = "UNKNOWN";
    }

    public InternalException(int status, String message, String catalog) {
        super(message);
        this.status = status;
        this.catalog = catalog;
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return ((message != null) ? (s + ": " + message) : s) + "(status=" + status +
                ", catalog=" + catalog + ")";
    }

}
