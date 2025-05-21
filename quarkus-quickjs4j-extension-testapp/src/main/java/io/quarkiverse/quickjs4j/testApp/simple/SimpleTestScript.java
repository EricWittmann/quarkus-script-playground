package io.quarkiverse.quickjs4j.testApp.simple;

import io.quarkiverse.quickjs4j.annotations.ScriptImplementation;
import io.roastedroot.quickjs4j.annotations.ScriptInterface;

@ScriptInterface
@ScriptImplementation(location = "src/main/ts/dist/simpleTestLibrary.js")
public interface SimpleTestScript {

    String getActualTestData();

}
