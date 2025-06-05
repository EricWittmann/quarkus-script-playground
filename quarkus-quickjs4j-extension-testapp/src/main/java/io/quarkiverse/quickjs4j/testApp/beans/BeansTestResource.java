package io.quarkiverse.quickjs4j.testApp.beans;

import io.quarkiverse.quickjs4j.testApp.beans.data.AddressBean;
import io.quarkiverse.quickjs4j.util.ScriptInterfaceUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/beansTest")
public class BeansTestResource {

    @ConfigProperty(name = "test-resource.script-path", defaultValue = "src/main/ts/dist/beansTestLibrary.js")
    String scriptPath;

    @Inject
    BeansTestScript testScript;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/script")
    public String testTheScript() {
        AddressBean home = new AddressBean();
        home.setCity("San Francisco");
        home.setState("CA");
        home.setStreet("123 Main Street");
        home.setZip("97227");

        StringBuilder builder = new StringBuilder();
        builder.append("PERSON:");
        builder.append(testScript.personAt(home));
        return builder.toString();
    }
}