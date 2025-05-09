package io.apicurio.generated;

import io.apicurio.calculator.CalculatorContext;
import io.roastedroot.quickjs4j.annotations.Builtins;
import io.roastedroot.quickjs4j.annotations.HostFunction;

@Builtins("CalculatorContext")
public class CalculatorContext_HostFunctions {

    private final CalculatorContext delegate;

    public CalculatorContext_HostFunctions(CalculatorContext delegate) {
        this.delegate = delegate;
    }

    @HostFunction
    public void log(String message) {
        delegate.log(message);
    }

}
