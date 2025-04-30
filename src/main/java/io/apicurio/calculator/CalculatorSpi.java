package io.apicurio.calculator;

import io.apicurio.annotations.JsInterface;

@JsInterface(script = "src/main/ts/Calculator.js", jsModule = CalculatorContext.class)
public interface CalculatorSpi {

    int add(int term1, int term2);

    int subtract(int term1, int term2);

    int multiply(int factor1, int factor2);

    int divide(int dividend, int divisor) throws DivideByZeroException;

}
