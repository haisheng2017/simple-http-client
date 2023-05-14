package hao.common.http.request;

import hao.common.http.auth.Credentials;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class InternalRequest {

    private final Credentials credentials;
    private final StringBuilder path;

    @Getter
    private final Map<String, String> parameters = new HashMap<>();
    @Getter
    private final Map<String, String> headers = new HashMap<>();

    private InternalHttpMethod httpMethod;
    @Getter
    private Object requestBody;

    public InternalRequest(Credentials credentials) {
        path = new StringBuilder();
        this.credentials = credentials;
    }

    public InternalRequest addHeader(String key, String value) {
        headers.put(key, value);
        return this;
    }

    public InternalRequest addParameters(String key, String value) {
        parameters.put(key, value);
        return this;
    }

    public InternalRequest get() {
        httpMethod = HttpMethod.GET;
        return this;
    }

    public InternalRequest post() {
        httpMethod = HttpMethod.POST;
        return this;
    }

    public InternalRequest post(Object requestBody) {
        httpMethod = HttpMethod.POST;
        this.requestBody = requestBody;
        return this;
    }

    public InternalRequest put() {
        httpMethod = HttpMethod.PUT;
        return this;
    }

    public InternalRequest put(Object requestBody) {
        httpMethod = HttpMethod.PUT;
        this.requestBody = requestBody;
        return this;
    }

    public InternalRequest delete() {
        httpMethod = HttpMethod.DELETE;
        return this;
    }

    public InternalRequest path(String path) {
        this.path.append(path);
        return this;
    }

    public String getPath() {
        return path.toString();
    }

    public String getMethod() {
        return httpMethod.getName();
    }
}
