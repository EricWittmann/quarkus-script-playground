package io.apicurio.app;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

@QuarkusMain(name = "AppMain")
public class AppMain {
    public static void main(String... args) {
        Quarkus.run(args);
    }
}
