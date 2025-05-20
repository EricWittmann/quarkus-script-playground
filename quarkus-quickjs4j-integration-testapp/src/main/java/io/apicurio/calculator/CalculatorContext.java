package io.apicurio.calculator;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CalculatorContext {

    public void log(String message) {
        System.out.println("LOG>> " + message);
    }

}
