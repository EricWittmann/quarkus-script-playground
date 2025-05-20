package io.quarkiverse.quickjs4j.testApp;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class SimpleTestResourceTest {

    @Test
    public void testScript() {
        String expected = "testTheScript::simple-test-data";
        RestAssured.given()
                .when().get("/simpleTest/script")
                .then()
                .statusCode(200)
                .contentType("text/plain")
                .body(is(expected));
    }
    @Test
    public void testFactory() {
        String expected = "testTheFactory::simple-test-data";
        RestAssured.given()
                .when().get("/simpleTest/factory")
                .then()
                .statusCode(200)
                .contentType("text/plain")
                .body(is(expected));
    }
}
