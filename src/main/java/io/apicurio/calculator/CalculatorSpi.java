package io.apicurio.calculator;

import io.apicurio.annotations.JsInterface;
import io.roastedroot.quickjs4j.annotations.GuestFunction;
import io.roastedroot.quickjs4j.annotations.Invokables;

@Invokables
public interface CalculatorSpi {

    @GuestFunction
    int add(int term1, int term2);

    @GuestFunction
    int subtract(int term1, int term2);

    @GuestFunction
    int multiply(int factor1, int factor2);

    @GuestFunction
    int divide(int dividend, int divisor) throws DivideByZeroException;

}
