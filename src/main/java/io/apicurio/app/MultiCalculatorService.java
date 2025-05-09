package io.apicurio.app;

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
            return proxy.add(term1, term2);
        }
    }

    public int subtract(String scriptName, int term1, int term2) throws Exception {
        try (var proxy = calculatorProducer.produceCalculatorProxy(toScriptPath(scriptName))) {
            return proxy.subtract(term1, term2);
        }
    }

    private static Path toScriptPath(String scriptName) {
        String workingDir = System.getProperty("user.dir");
        Path fullScriptPath = Paths.get(workingDir, "src/main/ts/dist/", scriptName + ".js");
        return fullScriptPath;
    }
}
