package hao.common.http.interceptor;

import hao.common.http.auth.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class BceRequestInterceptor implements Interceptor {

    private static final String BCE_AUTH_VERSION = "bce-auth-v1";
    private static final String MUST_SIGN_HEADER = "Host";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private final Credentials credentials;
    private final String host;
    private final Integer expirationPeriodInSeconds;

    public BceRequestInterceptor(Credentials credentials, String host) {
        this.credentials = credentials;
        this.host = host;
        expirationPeriodInSeconds = 60;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    private String toUTCString() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-ddThh:mm:ssZ");
        return df.format(new Date());
    }

    private String validPath(String path) {
        return (path == null || path.length() == 0) ? "/" : path;
    }

    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String canonicalQueryString(Map<String, String> queries) {
        return queries.entrySet().stream().filter(e -> !"authorization".equals(e.getKey())).map(e -> {
            if (e.getValue() == null) {
                return urlEncode(e.getKey());
            }
            return String.join("=", urlEncode(e.getKey()), urlEncode(e.getValue()));
        }).sorted().collect(Collectors.joining("&"));
    }

    private String canonicalHeaders(Map<String, String> headers) {
        return headers.entrySet().stream().map(e -> String.join(":", urlEncode(e.getKey().toLowerCase()), urlEncode(e.getValue().trim()))).sorted().collect(Collectors.joining("\n"));
    }

    private String signedHeaders(Map<String, String> headers) {
        return headers.keySet().stream().map(i -> urlEncode(i.toLowerCase())).sorted().collect(Collectors.joining(";"));
    }

    private Request sign(Request request) {
        if (credentials == null) {
            return request;
        }
        Request.Builder compressed = request.newBuilder();

        String ak = credentials.getAccessKey();
        String sk = credentials.getSecretKey();

        if (isEmpty(request.header(MUST_SIGN_HEADER))) {
            compressed.header(MUST_SIGN_HEADER, host);
        }

        String authStringPrefix = BCE_AUTH_VERSION + "/" + ak + "/" + toUTCString() + "/" + expirationPeriodInSeconds;
        String canonicalURI = urlEncode(validPath(path));
        String canonicalQueryString = canonicalQueryString(queries);
        String canonicalHeaders = canonicalHeaders(headers);
        String signedHeaders = signedHeaders(headers);
        String signingKey = hmacSha256Hex(sk, authStringPrefix);
        String canonicalRequest = String.join("\n", method.getName(), canonicalURI, canonicalQueryString, canonicalHeaders);
        String signature = hmacSha256Hex(signingKey, canonicalRequest);

        String authorization = String.join("/", authStringPrefix, signedHeaders, signature;

        compressed.header(AUTHORIZATION_HEADER, authorization);
        return compressed.build();
    }

    private String hmacSha256Hex(String sk, String authStringPrefix) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(sk.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return bytesToHex(mac.doFinal(authStringPrefix.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    @NotNull
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        return chain.proceed(sign(originalRequest));
    }
}
