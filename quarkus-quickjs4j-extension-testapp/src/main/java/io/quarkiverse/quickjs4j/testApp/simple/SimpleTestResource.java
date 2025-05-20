package io.quarkiverse.quickjs4j.testApp.simple;

import io.quarkiverse.quickjs4j.runtime.ScriptInterfaceFactory;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Paths;

@Path("/simpleTest")
public class SimpleTestResource {

    @ConfigProperty(name = "simple-test-resource.script-path", defaultValue = "src/main/ts/dist/simpleTestLibrary.js")
    String scriptPath;

    @Inject
    SimpleTestScript simpleTestScript;

    @Inject
    ScriptInterfaceFactory<SimpleTestScript, SimpleTestScriptCtx> factory;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/script")
    public String testTheScript() {
        return "testTheScript::" + simpleTestScript.getActualTestData();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/factory")
    public String testTheFactory() throws Exception {
        SimpleTestScript testScript = factory.create(Paths.get(scriptPath), null);
        try {
            return "testTheFactory::" + testScript.getActualTestData();
        } finally {
            ((AutoCloseable) testScript).close();
        }
    }
}