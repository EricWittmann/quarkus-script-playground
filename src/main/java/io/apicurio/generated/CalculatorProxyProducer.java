package io.apicurio.generated;

import io.apicurio.annotations.JsInterface;
import io.apicurio.calculator.Calculator;
import io.apicurio.calculator.CalculatorContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class CalculatorProxyProducer {

    @Inject
    CalculatorContext context;

    @Produces
    public CalculatorProxy produceCalculatorProxy() throws Exception {
        Class<?> proxyClass = CalculatorProxy.class;
        JsInterface jsInterfaceAnnotation = proxyClass.getAnnotation(JsInterface.class);
        if (jsInterfaceAnnotation == null) {
            throw new RuntimeException("Missing @JsInterface annotation");
        }

        String scriptPath = jsInterfaceAnnotation.script();
        String workingDir = System.getProperty("user.dir");
        Path fullScriptPath = Paths.get(workingDir, scriptPath);

        return produceCalculatorProxy(fullScriptPath);
    }

    public CalculatorProxy produceCalculatorProxy(Path fullScriptPath) throws Exception {
        System.out.println("Script path: " + fullScriptPath);
        String script = Files.readString(fullScriptPath);

        return new CalculatorProxy(script, context);
    }

}
