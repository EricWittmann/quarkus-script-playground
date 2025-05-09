package io.apicurio.calculator;

import io.roastedroot.quickjs4j.annotations.Builtins;
import io.roastedroot.quickjs4j.annotations.HostFunction;

@Builtins
public class CalculatorContext {

    @HostFunction
    public void log(String message) {
        System.out.println("LOG>> " + message);
    }

}
