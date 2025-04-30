import { CalculatorSpi } from "../../../generated/CalculatorSpi";

/// <reference path="../../../generated/CalculatorContext" />

class Calculator implements CalculatorSpi {

    add(term1: number, term2: number): number {
        log("Adding " + term1 + " to " + term2);
        return term1 + term2;
    }

    subtract(term1: number, term2: number): number {
        log("Subtracting " + term2 + " from " + term1);
        return term1 - term2;
    }

    multiply(factor1: number, factor2: number): number {
        log("Multiplying " + factor1 + " and " + factor2);
        return factor1 * factor2;
    }

    divide(dividend: number, divisor: number): number {
        log("Dividing " + dividend + " by " + divisor);
        return divisor / divisor;
    }

}

var calculator = new Calculator();
calculator;
