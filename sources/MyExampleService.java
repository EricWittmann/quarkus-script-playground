package io.apicurio.registry.script;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class MyExampleService {

    @Inject
    private CalculatorSpi calculator;

    @PostConstruct
    public void init() {
        // Simple example usage of the javascript Calculator implementation
        int twentyOne = calculator.multiply(3, 7);
        System.out.println("twentyOne: " + twentyOne);
    }

}
