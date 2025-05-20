package io.quarkiverse.quickjs4j.testApp;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class ContextTestResourceTest {

    @Test
    public void testScript() {
        String expected = "testTheScript::context-test-data::context-data";
        RestAssured.given()
                .when().get("/contextTest/script")
                .then()
                .statusCode(200)
                .contentType("text/plain")
                .body(is(expected));
    }
    @Test
    public void testFactory() {
        String expected = "testTheFactory::context-test-data::context-data";
        RestAssured.given()
                .when().get("/contextTest/factory")
                .then()
                .statusCode(200)
                .contentType("text/plain")
                .body(is(expected));
    }
}
