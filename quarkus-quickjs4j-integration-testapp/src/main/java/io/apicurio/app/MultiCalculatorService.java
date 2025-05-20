package io.apicurio.app;

import io.apicurio.calculator.CalculatorProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class MultiCalculatorService {

    @Inject
    private CalculatorProducer calculatorProducer;

    public int add(String scriptName, int term1, int term2) throws Exception {
        var calculator = calculatorProducer.produceCalculator(toScriptPath(scriptName));
        try {
            return calculator.add(term1, term2);
        } finally {
            ((AutoCloseable) calculator).close();
        }
    }

    public int subtract(String scriptName, int term1, int term2) throws Exception {
        var calculator = calculatorProducer.produceCalculator(toScriptPath(scriptName));
        try {
            return calculator.subtract(term1, term2);
        } finally {
            ((AutoCloseable) calculator).close();
        }
    }

    private static Path toScriptPath(String scriptName) {
        String workingDir = System.getProperty("user.dir");
        Path fullScriptPath = Paths.get(workingDir, "src/main/ts/dist/", scriptName + ".js");
        return fullScriptPath;
    }
}
