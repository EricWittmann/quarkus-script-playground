package io.quarkiverse.quickjs4j.testApp.beans;

import io.quarkiverse.quickjs4j.annotations.ScriptImplementation;
import io.quarkiverse.quickjs4j.testApp.beans.data.AddressBean;
import io.quarkiverse.quickjs4j.testApp.beans.data.PersonBean;
import io.quarkiverse.quickjs4j.testApp.context.ContextTestScriptCtx;
import io.roastedroot.quickjs4j.annotations.ScriptInterface;

@ScriptInterface(context = ContextTestScriptCtx.class)
@ScriptImplementation(location = "src/main/ts/dist/beansTestLibrary.js")
public interface BeansTestScript {

    PersonBean personAt(AddressBean address);

    void movePersonTo(PersonBean person, AddressBean address);

}
