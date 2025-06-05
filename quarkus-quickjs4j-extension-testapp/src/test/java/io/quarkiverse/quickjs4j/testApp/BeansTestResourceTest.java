package io.quarkiverse.quickjs4j.testApp;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class BeansTestResourceTest {

    @Test
    public void testScript() {
        String expected = "PERSON:PersonBean{name='Taylor Swift', age=35}";
        RestAssured.given()
                .when().get("/beansTest/script")
                .then()
                .statusCode(200)
                .contentType("text/plain")
                .body(is(expected));
    }

}