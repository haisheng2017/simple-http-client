package hao.common.http.interceptor;

import hao.common.http.auth.Credentials;
import okhttp3.HttpUrl;
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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

public class BceRequestInterceptor implements Interceptor {

    private static final String BCE_AUTH_VERSION = "bce-auth-v1";
    private static final String MUST_SIGN_HEADER = "Host";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    private final Credentials credentials;
    private final Integer expirationPeriodInSeconds;

    public BceRequestInterceptor(Credentials credentials) {
        this.credentials = credentials;
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
        return ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_INSTANT);
    }

    private String validPath(List<String> path) {
        return "/" + path.stream().map(this::urlEncode).collect(Collectors.joining("/"));
    }

    private String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.name()).replaceAll("\\+", "%20");
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
        return headers.entrySet().stream().filter(i -> i.getKey().equals(MUST_SIGN_HEADER))
                .map(e -> String.join(":", urlEncode(e.getKey().toLowerCase()), urlEncode(e.getValue().trim()))).sorted().collect(Collectors.joining("\n"));
    }

    private String signedHeaders(Map<String, String> headers) {
        return headers.keySet().stream().filter(i -> i.equals(MUST_SIGN_HEADER))
                .map(i -> urlEncode(i.toLowerCase())).sorted().collect(Collectors.joining(";"));
    }

    private Request sign(Request request) {
        if (credentials == null) {
            return request;
        }
        Request.Builder compressed = request.newBuilder();
        HttpUrl url = request.url();
        String ak = credentials.getAccessKey();
        String sk = credentials.getSecretKey();

        Map<String, String> headers = StreamSupport.stream(request.headers().spliterator(), false).collect(HashMap::new, (m, p) -> m.put(p.getFirst(), p.getSecond()), HashMap::putAll);
        if (isEmpty(headers.get(MUST_SIGN_HEADER))) {
            compressed.header(MUST_SIGN_HEADER, url.host());
            headers.put(MUST_SIGN_HEADER, url.host());
        }

        String authStringPrefix = BCE_AUTH_VERSION + "/" + ak + "/" + toUTCString() + "/" + expirationPeriodInSeconds;
        String canonicalURI = validPath(url.pathSegments());
        String canonicalQueryString = canonicalQueryString(
                IntStream.range(0, url.querySize()).collect(HashMap::new, (m, i) -> m.put(url.queryParameterName(i), url.queryParameterValue(i)), HashMap::putAll));
        String canonicalHeaders = canonicalHeaders(headers);
        String signedHeaders = signedHeaders(headers);
        String signingKey = hmacSha256Hex(sk, authStringPrefix);
        String canonicalRequest = String.join("\n", request.method(), canonicalURI, canonicalQueryString, canonicalHeaders);
        String signature = hmacSha256Hex(signingKey, canonicalRequest);

        String authorization = String.join("/", authStringPrefix, signedHeaders, signature);
        System.out.println(canonicalRequest);
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
