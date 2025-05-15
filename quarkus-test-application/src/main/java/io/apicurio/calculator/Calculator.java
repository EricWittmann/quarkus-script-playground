package io.apicurio.calculator;

@io.roastedroot.quickjs4j.annotations.ScriptInterface(context = CalculatorContext.class)
@io.apicurio.qqi.annotations.ScriptInterfaceProducer(script = "src/main/ts/dist/out.js", context = CalculatorContext.class)
public interface Calculator {

    int add(int term1, int term2);

    int subtract(int term1, int term2);

    int multiply(int factor1, int factor2);

    int divide(int dividend, int divisor) throws DivideByZeroException;

}
