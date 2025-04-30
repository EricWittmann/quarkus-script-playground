package io.apicurio.app;

import io.apicurio.calculator.CalculatorSpi;
import io.apicurio.generated.CalculatorSpiProducer;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class MultiCalculatorService {

    @Inject
    private CalculatorSpiProducer calculatorProducer;

    public int add(String scriptName, int term1, int term2) throws Exception {
        CalculatorSpi calculator = calculatorProducer.produceCalculatorSpi(toScriptPath(scriptName));
        return calculator.add(term1, term2);
    }

    public int subtract(String scriptName, int term1, int term2) throws Exception {
        CalculatorSpi calculator = calculatorProducer.produceCalculatorSpi(toScriptPath(scriptName));
        return calculator.subtract(term1, term2);
    }

    private static Path toScriptPath(String scriptName) {
        String workingDir = System.getProperty("user.dir");
        Path fullScriptPath = Paths.get(workingDir, "src/main/ts/", scriptName + ".js");
        return fullScriptPath;
    }
}
