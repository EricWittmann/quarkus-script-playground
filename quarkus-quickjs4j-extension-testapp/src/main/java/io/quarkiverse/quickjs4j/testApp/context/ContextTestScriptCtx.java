package io.quarkiverse.quickjs4j.testApp.context;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContextTestScriptCtx {

    public String getContextData() {
        return "context-data";
    }

}
