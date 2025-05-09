package io.apicurio.generated;

import io.apicurio.calculator.DivideByZeroException;
import io.roastedroot.quickjs4j.annotations.GuestFunction;
import io.roastedroot.quickjs4j.annotations.Invokables;

@Invokables
public interface Calculator_GuestFunctions {

    @GuestFunction
    int add(int term1, int term2);

    @GuestFunction
    int subtract(int term1, int term2);

    @GuestFunction
    int multiply(int factor1, int factor2);

    @GuestFunction
    int divide(int dividend, int divisor) throws DivideByZeroException;

}
