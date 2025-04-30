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
        System.out.println("---");
        Class<?> spiClass = CalculatorSpi.class;
        JsInterface jsInterfaceAnnotation = spiClass.getAnnotation(JsInterface.class);
        if (jsInterfaceAnnotation == null) {
            throw new RuntimeException("Missing @JsInterface annotation");
        }

        String scriptPath = jsInterfaceAnnotation.script();
        Class<?> contextClass = jsInterfaceAnnotation.jsModule();

        String workingDir = System.getProperty("user.dir");
        Path fullScriptPath = Paths.get(workingDir, scriptPath);
        String script = Files.readString(fullScriptPath);

        System.out.println("Script path: " + fullScriptPath);
        System.out.println("Context class: " + contextClass.getName());
        System.out.println("Script: \n" + script);

        CalculatorProxy proxy = new CalculatorProxy(script);
        System.out.println("---");
        return proxy;
    }

}
