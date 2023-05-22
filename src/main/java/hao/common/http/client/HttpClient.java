package hao.common.http.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import hao.common.http.exception.InternalException;
import hao.common.http.interceptor.JsonRequestInterceptor;
import hao.common.http.request.InternalRequest;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class HttpClient {

    public static ThreadLocal<InternalRequest> threadLocalRequest = new ThreadLocal<>();

    public static final MediaType MEDIA_JSON = MediaType.parse("application/json; charset=utf-8");
    private final URI endpoint;
    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HttpClient(String endpoint) throws URISyntaxException {
        this.endpoint = new URI(endpoint);
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        addInterceptors(builder);
        httpClient = builder.build();
    }

    protected void addInterceptors(OkHttpClient.Builder builder) {
        builder.addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY));
        builder.addInterceptor(new JsonRequestInterceptor());
    }

    protected InternalRequest createRequest() {
        return new InternalRequest();
    }

    protected <T> T execute(InternalRequest request, Class<T> responseClass) {
        try {
            Response response = httpClient.newCall(genOkHttpRequest(request)).execute();
            return handleResponse(response, responseClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T handleResponse(Response response, Class<T> responseClass) {
        if (response.code() != 200) {
            throw new InternalException(response.code(), fetchBody(response.body()));
        }
        try {
            return objectMapper.readValue(fetchBody(response.body()), responseClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String fetchBody(ResponseBody body) {
        if (body == null) {
            return "";
        }
        try {
            return body.string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Request genOkHttpRequest(InternalRequest request) {
        Request.Builder builder = new Request.Builder();
        HttpUrl.Builder url = HttpUrl.get(this.endpoint).newBuilder();
        url.addPathSegments(request.getPath());
        request.getHeaders().forEach(builder::addHeader);
        request.getParameters().forEach(url::addQueryParameter);
        RequestBody body = null;
        if (request.getRequestBody() != null) {
            try {
                body = RequestBody.create(objectMapper.writeValueAsBytes(request.getRequestBody()), MEDIA_JSON);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (request.getMethod() == null || "".equals(request.getMethod())) {
            throw new RuntimeException("request method is empty");
        }
        return builder.url(url.build()).method(request.getMethod(), body).build();
    }
}
