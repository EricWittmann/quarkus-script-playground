package io.apicurio.calculator;

import io.apicurio.annotations.JsInterface;

@JsInterface(script = "src/main/ts/dist/out.js", context = CalculatorContext.class)
public interface Calculator {

    int add(int term1, int term2);

    int subtract(int term1, int term2);

    int multiply(int factor1, int factor2);

    int divide(int dividend, int divisor) throws DivideByZeroException;

}
