package io.apicurio.app.rest;

import io.apicurio.app.MultiCalculatorService;
import io.apicurio.app.SingleCalculatorService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/test")
public class TestResource {

    @Inject
    SingleCalculatorService scs;
    @Inject
    MultiCalculatorService mcs;

    @GET
    @Path("/single")
    @Produces(MediaType.TEXT_PLAIN)
    public String single() throws Exception {
        return "Hello from the test endpoint (single): " + scs.twentyOne();
    }

    @GET
    @Path("/multi")
    @Produces(MediaType.TEXT_PLAIN)
    public String multi() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Hello from the test endpoint (multi): \n");
        sb.append("  script1(10 + 7): " + mcs.add("script1", 10, 7));
        sb.append("\n");
        sb.append("  script2(13 + 4): " + mcs.add("script2", 13, 4));
        sb.append("\n");
        sb.append("  script3(20 - 3): " + mcs.subtract("script3", 20, 3));
        sb.append("\n");

        return sb.toString();
    }
}
