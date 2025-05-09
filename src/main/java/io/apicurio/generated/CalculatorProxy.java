package io.apicurio.generated;

import io.apicurio.annotations.JsInterface;
import io.apicurio.calculator.CalculatorContext;
import io.apicurio.calculator.CalculatorContext_Builtins;
import io.apicurio.calculator.CalculatorSpi;
import io.apicurio.calculator.CalculatorSpi_Invokables;
import io.apicurio.calculator.DivideByZeroException;
import io.roastedroot.quickjs4j.core.Builtins;
import io.roastedroot.quickjs4j.core.Engine;
import io.roastedroot.quickjs4j.core.Runner;

@JsInterface(script = "src/main/ts/dist/out.js")
public class CalculatorProxy implements AutoCloseable {

    private final CalculatorSpi spi;
    private final Runner runner;

    public CalculatorProxy(String script) {
        CalculatorContext context = new CalculatorContext();
        Engine engine = Engine.builder()
                .addBuiltins(CalculatorContext_Builtins.toBuiltins(context))
                .addInvokables(CalculatorSpi_Invokables.toInvokables())
                .build();
        this.runner = Runner.builder().withEngine(engine).build();
        this.spi = CalculatorSpi_Invokables.create(script, runner);
    }

    public CalculatorSpi spi() {
        return spi;
    }

    @Override
    public void close() {
        runner.close();
    }
}
