package io.quarkiverse.quickjs4j.testApp.simple;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SimpleTestScriptCtx {

    public String getContextData() {
        return "context-data";
    }

}
