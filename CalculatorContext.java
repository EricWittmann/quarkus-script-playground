package io.apicurio.registry.script;

import io.roastedroot.quickjs4j.annotations.HostFunction;
import io.roastedroot.quickjs4j.annotations.JsModule;

@JsModule
public class CalculatorContext {

    @HostFunction("log")
    public void log(String message) {
        System.out.println(message);
    }

}
