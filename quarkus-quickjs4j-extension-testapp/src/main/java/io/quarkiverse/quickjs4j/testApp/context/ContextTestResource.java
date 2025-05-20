package io.quarkiverse.quickjs4j.testApp.context;

import io.quarkiverse.quickjs4j.runtime.ScriptInterfaceFactory;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.file.Paths;

@Path("/contextTest")
public class ContextTestResource {

    @ConfigProperty(name = "test-resource.script-path", defaultValue = "src/main/ts/dist/contextTestLibrary.js")
    String scriptPath;

    @Inject
    ContextTestScript testScript;

    @Inject
    ContextTestScriptCtx context;

    @Inject
    ScriptInterfaceFactory<ContextTestScript, ContextTestScriptCtx> factory;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/script")
    public String testTheScript() {
        return "testTheScript::" + testScript.getActualTestData();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/factory")
    public String testTheFactory() throws Exception {
        ContextTestScript testScript = factory.create(Paths.get(scriptPath), context);
        try {
            return "testTheFactory::" + testScript.getActualTestData();
        } finally {
            ((AutoCloseable) testScript).close();
        }
    }
}