// calculatorImpl.js (JavaScript library implementing the Calculator SPI)
import { CalculatorSpi } from "calculator-spi";
import * as CalculatorContext from "calculator-context";

export class CalculatorImpl implements CalculatorSpi {

    add(term1: number, term2: number): number {
        CalculatorContext.log("Adding " + term1 + " to " + term2);
        return term1 + term2;
    }

    subtract(term1: number, term2: number): number {
        CalculatorContext.log("Subtracting " + term2 + " from " + term1);
        return term1 - term2;
    }

    multiply(factor1: number, factor2: number): number {
        CalculatorContext.log("Multiplying " + factor1 + " and " + factor2);
        return factor1 * factor2;
    }

    divide(dividend: number, divisor: number): number {
        CalculatorContext.log("Dividing " + dividend + " by " + divisor);
        return divisor / divisor;
    }

}
