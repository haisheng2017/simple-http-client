package hao.common.http.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class JsonRequestInterceptor implements Interceptor {
    @NotNull
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request.Builder compressed = originalRequest.newBuilder();
        if (originalRequest.header("Content-Type") == null) {
            compressed.header("Content-Type", "application/json; charset=utf-8");
        }
        if (originalRequest.header("Accept") == null) {
            compressed.header("Accept", "application/json");
        }
        return chain.proceed(compressed.build());
    }
}
