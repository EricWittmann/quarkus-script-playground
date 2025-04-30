package io.apicurio.generated;

import io.apicurio.calculator.CalculatorContext;
import io.apicurio.calculator.CalculatorContext_Builtins;
import io.apicurio.calculator.CalculatorSpi;
import io.apicurio.calculator.DivideByZeroException;
import io.roastedroot.quickjs4j.core.Builtins;
import io.roastedroot.quickjs4j.core.Engine;
import io.roastedroot.quickjs4j.core.Runner;

public class CalculatorProxy implements CalculatorSpi {

    private final String script;

    public CalculatorProxy(String script) {
        this.script = script;
    }

    private Runner createRunner(CalculatorReturnValue utils) {
        CalculatorContext context = new CalculatorContext();
        Builtins builtins = Builtins.builder()
                .add(CalculatorContext_Builtins.toBuiltins(context))
                .add(CalculatorReturnValue_Builtins.toBuiltins(utils))
                .build();
        Engine engine = Engine.builder().withBuiltins(builtins).build();
        Runner runner = Runner.builder().withEngine(engine).build();
        return runner;
    }

    private int execute(String expression) {
        try {
            CalculatorReturnValue rval = new CalculatorReturnValue();
            Runner runner = createRunner(rval);
            runner.compileAndExec(script + "\n" + expression);
            return (Integer) rval.getReturnValue();
//            System.out.println("===> return value 1: " + rval.getReturnValue());
//            runner.compileAndExec("setReturnValue('foo');");
//            System.out.println("===> return value 2: " + rval.getReturnValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int add(int term1, int term2) {
        String expression = String.format("setReturnValue(calculator.add(%d, %d));", term1, term2);
        return execute(expression);
    }

    @Override
    public int subtract(int term1, int term2) {
        String expression = String.format("setReturnValue(calculator.subtract(%d, %d));", term1, term2);
        return execute(expression);
    }

    @Override
    public int multiply(int factor1, int factor2) {
        String expression = String.format("setReturnValue(calculator.multiply(%d, %d));", factor1, factor2);
        return execute(expression);
    }

    @Override
    public int divide(int dividend, int divisor) throws DivideByZeroException {
        String expression = String.format("setReturnValue(calculator.divide(%d, %d));", dividend, divisor);
        return execute(expression);
    }

}
