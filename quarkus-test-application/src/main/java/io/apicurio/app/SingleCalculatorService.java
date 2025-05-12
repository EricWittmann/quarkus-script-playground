package io.apicurio.app;

import io.apicurio.calculator.Calculator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SingleCalculatorService {

    @Inject
    private Calculator calculator;

    public int twentyOne() {
        return calculator.multiply(3, 7);
    }
}
