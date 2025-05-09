package io.apicurio.generated;

import io.apicurio.calculator.Calculator;
import io.apicurio.calculator.CalculatorContext;
import io.apicurio.calculator.DivideByZeroException;
import io.roastedroot.quickjs4j.core.Engine;
import io.roastedroot.quickjs4j.core.Runner;

public class CalculatorProxy implements Calculator, AutoCloseable {

    private final Calculator_GuestFunctions spi;
    private final Runner runner;

    public CalculatorProxy(String script, CalculatorContext context) {
        CalculatorContext_HostFunctions hostFunctions = new CalculatorContext_HostFunctions(context);
        Engine engine = Engine.builder()
                .addBuiltins(CalculatorContext_HostFunctions_Builtins.toBuiltins(hostFunctions))
                .addInvokables(Calculator_GuestFunctions_Invokables.toInvokables())
                .build();
        this.runner = Runner.builder().withEngine(engine).build();
        this.spi = Calculator_GuestFunctions_Invokables.create(script, runner);
    }

    @Override
    public void close() {
        runner.close();
    }

    @Override
    public int add(int term1, int term2) {
        return this.spi.add(term1, term2);
    }

    @Override
    public int subtract(int term1, int term2) {
        return this.spi.subtract(term1, term2);
    }

    @Override
    public int multiply(int factor1, int factor2) {
        return this.spi.multiply(factor1, factor2);
    }

    @Override
    public int divide(int dividend, int divisor) throws DivideByZeroException {
        return this.spi.divide(dividend, divisor);
    }
}
