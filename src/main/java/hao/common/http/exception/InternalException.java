package hao.common.http.exception;

import java.io.IOException;

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

    public InternalException(int status, Throwable throwable) {
        super(throwable);
        this.status = status;
        this.catalog = throwable.getClass().getSimpleName();
    }

    public static InternalException ioException(IOException ex) {
        return new InternalException(1, ex);
    }

    public static InternalException badUsage(String msg) {
        return new InternalException(2, msg, "BadUsage");
    }

    public static InternalException runtimeError(String msg) {
        return new InternalException(3, msg, "RuntimeError");
    }

    @Override
    public String toString() {
        String s = getClass().getName();
        String message = getLocalizedMessage();
        return ((message != null) ? (s + ": " + message) : s) + "(status=" + status +
                ", catalog=" + catalog + ")";
    }

}
