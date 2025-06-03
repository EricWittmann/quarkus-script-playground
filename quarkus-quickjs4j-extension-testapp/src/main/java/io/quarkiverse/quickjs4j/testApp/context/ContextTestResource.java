package io.quarkiverse.quickjs4j.testApp.context;

import io.quarkiverse.quickjs4j.ScriptInterfaceFactory;
import io.quarkiverse.quickjs4j.testApp.ScriptLibraryUtils;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
        String scriptLibrary = ScriptLibraryUtils.loadScriptLibrary(scriptPath);
        ContextTestScript testScript = factory.create(scriptLibrary, context);
        try {
            return "testTheFactory::" + testScript.getActualTestData();
        } finally {
            ((AutoCloseable) testScript).close();
        }
    }
}