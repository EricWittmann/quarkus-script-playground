package io.apicurio.app;

import io.apicurio.calculator.CalculatorSpi;
import io.apicurio.generated.CalculatorProxy;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SingleCalculatorService {

    @Inject
    private CalculatorProxy calculatorProxy;

    @PostConstruct
    public void init() {
    }

    public int twentyOne() {
        return calculatorProxy.spi().multiply(3, 7);
    }
}
