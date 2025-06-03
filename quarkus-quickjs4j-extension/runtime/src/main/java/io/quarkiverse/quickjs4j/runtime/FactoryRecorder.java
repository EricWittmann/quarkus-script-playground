package io.quarkiverse.quickjs4j.runtime;

import io.quarkus.runtime.annotations.Recorder;

import java.util.function.Supplier;

@Recorder
public class FactoryRecorder {

    public Supplier<Object> createFactorySupplier(String className) {
        return new Supplier<>() {
            @Override
            public Object get() {
                try {
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create factory: " + className, e);
                }
            }
        };
    }
}
