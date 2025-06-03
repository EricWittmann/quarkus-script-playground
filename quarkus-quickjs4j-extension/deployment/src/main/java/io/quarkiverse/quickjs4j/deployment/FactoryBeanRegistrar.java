package io.quarkiverse.quickjs4j.deployment;

import io.quarkus.arc.processor.BeanRegistrar;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import jakarta.enterprise.context.ApplicationScoped;

public class FactoryBeanRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistrar.RegistrationContext registrationContext) {
        Type factoryType = ParameterizedTypeImpl.of(MyFactory.class, MyType.class, MyContext.class);

        registrationContext.configure(factoryType)
                .types(factoryType)
                .scope(ApplicationScoped.class)
                .creator(mc -> {
                    // Use Gizmo MethodCreator to instantiate the generated class
                    ResultHandle instance = mc.newInstance(
                            MethodDescriptor.ofConstructor("com.example.GeneratedMyFactory"));
                    mc.returnValue(instance);
                })
                .done();
    }

}
