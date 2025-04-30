package io.apicurio.app;

import io.apicurio.calculator.CalculatorSpi;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SingleCalculatorService {

    @Inject
    private CalculatorSpi calculator;

    @PostConstruct
    public void init() {
    }

    public int twentyOne() {
        return calculator.multiply(3, 7);
    }
}
