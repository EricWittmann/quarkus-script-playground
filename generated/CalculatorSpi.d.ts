export interface CalculatorSpi {
    add(term1: number, term2: number): number;
    subtract(term1: number, term2: number): number;
    multiply(factor1: number, factor2: number): number;
    divide(dividend: number, divisor: number): number;
}
