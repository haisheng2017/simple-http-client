package hao.common.http.request;

public enum HttpMethod implements InternalHttpMethod {
    GET, POST, PUT, DELETE;

    public String getName() {
        return this.name();
    }
}
