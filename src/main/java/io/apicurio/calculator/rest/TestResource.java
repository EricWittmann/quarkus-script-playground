package io.apicurio.calculator.rest;

import io.apicurio.calculator.MyExampleService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test")
public class TestResource {

    @Inject
    MyExampleService mes;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {
        return "Hello from the test endpoint: " + mes.twentyOne();
    }
}
