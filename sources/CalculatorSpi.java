package io.apicurio.registry.script;

@JavascriptSpi(script = "/tmp/calculatorImpl.ts", jsModule = CalculatorContext.class)
public interface CalculatorSpi {

    public int add(int term1, int term2);

    public int subtract(int term1, int term2);

    public int multiply(int factor1, int factor2);

    public int divide(int dividend, int divisor) throws DivideByZeroException;

}
