package hao.common.http.exception;

public class InternalException extends RuntimeException {
    private final int code;
    private final String catalog;

    public InternalException(int code, String message) {
        super(message);
        this.code = code;
        catalog = "UNKNOWN";
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return ((message != null) ? (s + ": " + message) : s) + "(code=" + code +
                ", catalog=" + catalog + ")";
    }

}
