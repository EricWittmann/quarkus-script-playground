package io.quarkiverse.quickjs4j.testApp.context;

import io.quarkiverse.quickjs4j.annotations.ScriptImplementation;
import io.roastedroot.quickjs4j.annotations.ScriptInterface;

@ScriptInterface(context = ContextTestScriptCtx.class)
@ScriptImplementation(location = "src/main/ts/dist/contextTestLibrary.js")
public interface ContextTestScript {

    String getActualTestData();

}
