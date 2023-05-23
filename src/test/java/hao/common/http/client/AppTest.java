package hao.common.http.client;

import hao.common.http.auth.BceCredentials;
import hao.common.http.interceptor.BceRequestInterceptor;
import hao.common.http.interceptor.JsonRequestInterceptor;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import okhttp3.logging.HttpLoggingInterceptor;

import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase {
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp() {
        assertTrue(true);
    }

    public void testHttpClient() throws URISyntaxException {
        String testEndpoint = "http://baike.baidu.com";
        HttpClient myHttp = new HttpClient(testEndpoint) {
        };
        Object ret = myHttp.execute(
                myHttp.createRequest().path("api/openapi/BaikeLemmaCardApi")
                        .addParameters("format", "json").get(), Object.class);
        System.out.println(ret);
    }

    public void testBceHttpClient() throws URISyntaxException {
        String endpoint = "http://iam.bj.baidubce.com";
        String ak = "";
        String sk = "";
        HttpClient bce = new HttpClient(endpoint, Arrays.asList(new JsonRequestInterceptor(),
                new BceRequestInterceptor(new BceCredentials(ak, sk)),
                new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))) {
        };
        Object ret = bce.execute(bce.createRequest()
                .path("v1/user")
                .get(), Object.class
        );
        System.out.println(ret);
    }
}
