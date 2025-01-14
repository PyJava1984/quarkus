package org.jboss.resteasy.reactive.server.vertx.test.resource.basic;

import io.restassured.RestAssured;
import java.util.function.Supplier;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.Matchers;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.vertx.test.framework.ResteasyReactiveUnitTest;
import org.jboss.resteasy.reactive.server.vertx.test.resource.basic.resource.GetAbsolutePathResource;
import org.jboss.resteasy.reactive.server.vertx.test.resource.basic.resource.UriInfoEncodedQueryResource;
import org.jboss.resteasy.reactive.server.vertx.test.resource.basic.resource.UriInfoEncodedTemplateResource;
import org.jboss.resteasy.reactive.server.vertx.test.resource.basic.resource.UriInfoEscapedMatrParamResource;
import org.jboss.resteasy.reactive.server.vertx.test.resource.basic.resource.UriInfoQueryParamsResource;
import org.jboss.resteasy.reactive.server.vertx.test.resource.basic.resource.UriInfoRelativizeResource;
import org.jboss.resteasy.reactive.server.vertx.test.resource.basic.resource.UriInfoResolveResource;
import org.jboss.resteasy.reactive.server.vertx.test.resource.basic.resource.UriInfoSimpleResource;
import org.jboss.resteasy.reactive.server.vertx.test.resource.basic.resource.UriInfoSimpleSingletonResource;
import org.jboss.resteasy.reactive.server.vertx.test.simple.PortProviderUtil;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * @tpSubChapter Resources
 * @tpChapter Integration tests
 * @tpTestCaseDetails Tests for java.net.URI class
 * @tpSince RESTEasy 3.0.16
 */
@DisplayName("Uri Info Test")
public class UriInfoTest {

    protected final Logger logger = Logger.getLogger(UriInfoTest.class);

    private static Client client;

    @RegisterExtension
    static ResteasyReactiveUnitTest quarkusUnitTest = new ResteasyReactiveUnitTest()
            .setArchiveProducer(new Supplier<>() {
                @Override
                public JavaArchive get() {
                    JavaArchive war = ShrinkWrap.create(JavaArchive.class);
                    war.addClass(PortProviderUtil.class);
                    // Use of PortProviderUtil in the deployment
                    war.addClasses(UriInfoSimpleResource.class, UriInfoEncodedQueryResource.class,
                            UriInfoQueryParamsResource.class, UriInfoSimpleSingletonResource.class,
                            UriInfoEncodedTemplateResource.class, UriInfoEscapedMatrParamResource.class,
                            UriInfoEncodedTemplateResource.class, GetAbsolutePathResource.class,
                            UriInfoRelativizeResource.class, UriInfoResolveResource.class);
                    return war;
                }
            });

    @BeforeAll
    public static void before() throws Exception {
        client = ClientBuilder.newClient();
    }

    @AfterAll
    public static void after() throws Exception {
        client.close();
        client = null;
    }

    /**
     * @tpTestDetails Check uri from resource on server. Simple resource is used.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    @DisplayName("Test Uri Info")
    public void testUriInfo() throws Exception {
        basicTest("/simple", UriInfoSimpleResource.class.getSimpleName());
        basicTest("/simple/fromField", UriInfoSimpleResource.class.getSimpleName());

        RestAssured.get("/" + UriInfoSimpleResource.class.getSimpleName() + "/uri?foo=bar").then()
                .body(Matchers.endsWith("/uri?foo=bar"));
    }

    /**
     * @tpTestDetails Check uri from resource on server. Resource is set as singleton to RESTEasy.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    @Disabled
    @DisplayName("Test Uri Info With Singleton")
    public void testUriInfoWithSingleton() throws Exception {
        basicTest("/simple/fromField", UriInfoSimpleSingletonResource.class.getSimpleName());
    }

    /**
     * @tpTestDetails Check uri from resource on server. Test complex parameter.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    @Disabled
    @DisplayName("Test Escaped Matr Param")
    public void testEscapedMatrParam() throws Exception {
        basicTest("/queryEscapedMatrParam;a=a%3Bb;b=x%2Fy;c=m%5Cn;d=k%3Dl",
                UriInfoEscapedMatrParamResource.class.getSimpleName());
    }

    /**
     * @tpTestDetails Check uri from resource on server. Test space character in URI.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    @Disabled
    @DisplayName("Test Encoded Template Params")
    public void testEncodedTemplateParams() throws Exception {
        basicTest("/a%20b/x%20y", UriInfoEncodedTemplateResource.class.getSimpleName());
    }

    /**
     * @tpTestDetails Check uri from resource on server. Test space character in URI attribute.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    @Disabled
    @DisplayName("Test Encoded Query Params")
    public void testEncodedQueryParams() throws Exception {
        basicTest("/query?a=a%20b", UriInfoEncodedQueryResource.class.getSimpleName());
    }

    /**
     * @tpTestDetails Check uri from resource on server. Test return value from resource - same URI address.
     * @tpSince RESTEasy 3.0.16
     */
    @Test
    @DisplayName("Test Relativize")
    public void testRelativize() throws Exception {
        String uri = PortProviderUtil.generateURL("/");
        WebTarget target = client.target(uri);
        String result;
        result = target.path("a/b/c").queryParam("to", "a/d/e").request().get(String.class);
        Assertions.assertEquals("../../d/e", result);
        result = target.path("a/b/c").queryParam("to", UriBuilder.fromUri(uri).path("a/d/e").build().toString()).request()
                .get(String.class);
        Assertions.assertEquals(result, "../../d/e");
        result = target.path("a/b/c").queryParam("to", "http://foobar/a/d/e").request().get(String.class);
        Assertions.assertEquals(result, "http://foobar/a/d/e");
    }

    @Test
    @DisplayName("Test Resolve")
    public void testResolve() throws Exception {
        WebTarget target = client.target(PortProviderUtil.generateURL("/"));
        String result = target.path("resolve").queryParam("to", "a/d/e").request().get(String.class);
        Assertions.assertEquals(PortProviderUtil.generateURL("/a/d/e"), result);
    }

    private static void basicTest(String path, String testName) throws Exception {
        Response response = client.target(PortProviderUtil.generateURL("/" + testName + path)).request().get();
        try {
            Assertions.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        } finally {
            response.close();
        }
    }

    /**
     * @tpTestDetails Test that UriInfo.getQueryParameters() returns an immutable map. Test's logic is in end-point.
     * @tpSince RESTEasy 3.0.17
     */
    @Test
    @DisplayName("Test Query Params Mutability")
    public void testQueryParamsMutability() throws Exception {
        basicTest("/queryParams?a=a,b", "UriInfoQueryParamsResource");
    }

    @Test
    @DisplayName("Test Get Absolute Path")
    public void testGetAbsolutePath() throws Exception {
        doTestGetAbsolutePath("/absolutePath", "unset");
        doTestGetAbsolutePath("/absolutePath?dummy=1234", "1234");
        doTestGetAbsolutePath("/absolutePath?foo=bar&dummy=1234", "1234");
    }

    private void doTestGetAbsolutePath(String path, String expectedDummyHeader) {
        String absolutePathHeader = RestAssured.get(path)
                .then()
                .statusCode(200)
                .header("dummy", expectedDummyHeader).extract().header("absolutePath");
        org.assertj.core.api.Assertions.assertThat(absolutePathHeader).endsWith("/absolutePath");
    }
}
