package io.apicurio.generated;

import io.apicurio.annotations.JsInterface;
import io.apicurio.calculator.CalculatorSpi;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@ApplicationScoped
public class CalculatorSpiProducer {

    @Produces
    public CalculatorSpi produceCalculatorSpi() throws Exception {
        Class<?> spiClass = CalculatorSpi.class;
        JsInterface jsInterfaceAnnotation = spiClass.getAnnotation(JsInterface.class);
        if (jsInterfaceAnnotation == null) {
            throw new RuntimeException("Missing @JsInterface annotation");
        }

        String scriptPath = jsInterfaceAnnotation.script();
        String workingDir = System.getProperty("user.dir");
        Path fullScriptPath = Paths.get(workingDir, scriptPath);

        return produceCalculatorSpi(fullScriptPath);
    }

    public CalculatorSpi produceCalculatorSpi(Path fullScriptPath) throws Exception {
        System.out.println("Script path: " + fullScriptPath);
        String script = Files.readString(fullScriptPath);

        CalculatorProxy proxy = new CalculatorProxy(script);
        return proxy;
    }

}
