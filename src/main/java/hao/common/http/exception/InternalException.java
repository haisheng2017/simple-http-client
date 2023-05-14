package hao.common.http.exception;

public class InternalException extends RuntimeException {
    private final int code;
    private final String message;
    private final String catalog;

    public InternalException(int code, String message) {
        this.code = code;
        this.message = message;
        catalog = "UNKNOWN";
    }
}
