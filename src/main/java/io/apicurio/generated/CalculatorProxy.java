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
            CalculatorReturnValue utils = new CalculatorReturnValue();
            Runner runner = createRunner(utils);
            runner.compileAndExec(script + "\n" + expression);
            System.out.println("===> return value 1: " + utils.getReturnValue());
            runner.compileAndExec("setReturnValue('foo');");
            System.out.println("===> return value 2: " + utils.getReturnValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public int add(int term1, int term2) {
        String expression = """
                setReturnValue(calculator.add(TERM1, TERM2));
                """
                .replace("TERM1", String.valueOf(term1))
                .replace("TERM2", String.valueOf(term2));
        return execute(expression);
    }

    @Override
    public int subtract(int term1, int term2) {
        String expression = """
                setReturnValue(calculator.subtract(TERM1, TERM2));
                """
                .replace("TERM1", String.valueOf(term1))
                .replace("TERM2", String.valueOf(term2));
        return execute(expression);
    }

    @Override
    public int multiply(int factor1, int factor2) {
        String expression = """
                setReturnValue(calculator.multiply(FACTOR1, FACTOR2));
                """
                .replace("FACTOR1", String.valueOf(factor1))
                .replace("FACTOR2", String.valueOf(factor2));
        return execute(expression);
    }

    @Override
    public int divide(int dividend, int divisor) throws DivideByZeroException {
        String expression = """
                setReturnValue(calculator.divide(DIVIDEND, DIVISOR));
                """
                .replace("DIVIDEND", String.valueOf(dividend))
                .replace("DIVISOR", String.valueOf(divisor));
        return execute(expression);
    }

}
