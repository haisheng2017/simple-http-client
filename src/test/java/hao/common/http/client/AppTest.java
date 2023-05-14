package hao.common.http.client;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.URISyntaxException;

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
}
