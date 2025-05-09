package io.apicurio.app;

import io.apicurio.calculator.CalculatorSpi;
import io.apicurio.generated.CalculatorProxyProducer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class MultiCalculatorService {

    @Inject
    private CalculatorProxyProducer calculatorProducer;

    public int add(String scriptName, int term1, int term2) throws Exception {
        try (var proxy = calculatorProducer.produceCalculatorProxy(toScriptPath(scriptName))) {
            return proxy.spi().add(term1, term2);
        }
    }

    public int subtract(String scriptName, int term1, int term2) throws Exception {
        try (var proxy = calculatorProducer.produceCalculatorProxy(toScriptPath(scriptName))) {
            return proxy.spi().subtract(term1, term2);
        }
    }

    private static Path toScriptPath(String scriptName) {
        String workingDir = System.getProperty("user.dir");
        Path fullScriptPath = Paths.get(workingDir, "src/main/ts/", scriptName + ".js");
        return fullScriptPath;
    }
}
