package io.apicurio.generated;

import io.roastedroot.quickjs4j.annotations.HostFunction;
import io.roastedroot.quickjs4j.annotations.JsModule;

@JsModule
public class CalculatorReturnValue {

    private Object returnValue = 0;

    @HostFunction("setReturnValue")
    public void setReturnValue(Object returnValue) {
        System.out.println("===> setReturnValue(" + returnValue + ")");
        this.returnValue = returnValue;
    }

    public Object getReturnValue() {
        return returnValue;
    }

}
