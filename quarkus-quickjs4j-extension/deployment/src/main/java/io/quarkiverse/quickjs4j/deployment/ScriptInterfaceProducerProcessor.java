package io.quarkiverse.quickjs4j.deployment;

import io.quarkiverse.quickjs4j.annotations.ScriptImplementation;
import io.quarkiverse.quickjs4j.runtime.ScriptInterfaceFactory;
import io.quarkiverse.quickjs4j.runtime.ScriptUtils;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.roastedroot.quickjs4j.annotations.ScriptInterface;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.IndexView;

import java.util.Collection;

public class ScriptInterfaceProducerProcessor {

    private static final String FEATURE = "quickjs4j-cdi";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void build(BuildProducer<AdditionalIndexedClassesBuildItem> producer) {
        producer.produce(new AdditionalIndexedClassesBuildItem(ScriptInterfaceFactory.class.getName()));
    }


    @BuildStep
    void generateProducers(CombinedIndexBuildItem indexBuildItem,
                           BuildProducer<GeneratedBeanBuildItem> generatedBeans) {
        System.out.println(">>> generateProducers()");

        IndexView index = indexBuildItem.getIndex();
        Collection<AnnotationInstance> targetAnnotations = index.getAnnotations(ScriptInterface.class);

        for (AnnotationInstance annotation : targetAnnotations) {
            System.out.println(">>> generateProducers() :: annotation: " + annotation);
            if (annotation.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.CLASS) {
                String targetClassName = annotation.target().asClass().name().toString();
                String proxyClassName = targetClassName + "_Proxy";
                String factoryClassName = targetClassName + "Factory";
                String producerClassName = targetClassName + "Producer";

                ClassType contextClass = getContextClass(annotation);
                System.out.println(">>> generateProducers() :: contextClass: " + contextClass + " -- " + contextClass.getClass());

                String scriptLocation = getScriptLocation(annotation.target());
                System.out.println(">>> generateProducers() :: scriptLocation: " + scriptLocation);

                // Generate the producer class
                generateProducerClass(generatedBeans, targetClassName, proxyClassName, producerClassName,
                        factoryClassName, contextClass, scriptLocation);
            }
        }
    }

    // FIXME: handle the case where there is no context class (the class will be Void.class in that case)
    private ClassType getContextClass(AnnotationInstance annotation) {
        AnnotationValue value = annotation.value("context");
        return (ClassType) value.asClass();
    }

    private String getScriptLocation(AnnotationTarget target) {
        AnnotationInstance scriptAnnotation = target.annotation(ScriptImplementation.class);
        if (scriptAnnotation != null) {
            AnnotationValue value = scriptAnnotation.value("location");
            return value.asString();
        }
        return null;
    }

    private void generateProducerClass(BuildProducer<GeneratedBeanBuildItem> generatedBeans,
                                       String targetClassName, String proxyClassName, String producerClassName,
                                       String factoryClassName, ClassType contextClassType, String scriptLocation) {
        String contextClassName = contextClassType.name().toString();
        String scriptLibrary = null;
        if (scriptLocation != null) {
            scriptLibrary = ScriptUtils.loadScriptLibrary(scriptLocation);
        }

        ClassOutput classOutput = new GeneratedBeanGizmoAdaptor(generatedBeans);

        try (ClassCreator classCreator = ClassCreator.builder()
                .classOutput(classOutput)
                .className(producerClassName)
                .build()) {

            // The producer will be application scoped
            classCreator.addAnnotation(ApplicationScoped.class);

            // Static field for the default script (if provided)
            FieldDescriptor staticField = classCreator.getFieldCreator("DEFAULT_SCRIPT", String.class)
                    .setModifiers(0x9) // public static
                    .getFieldDescriptor();
            MethodCreator clinit = classCreator.getMethodCreator("<clinit>", "V"); // static initializer
            clinit.setModifiers(0x8); // static
            clinit.writeStaticField(staticField, clinit.load(scriptLibrary));
            clinit.returnValue(null);

            // Inject the context - only needed if a script location is configured
            if (scriptLocation != null) {
                FieldCreator contextField = classCreator.getFieldCreator("context", contextClassName);
                contextField.setModifiers(0x1); // public
                contextField.addAnnotation(Inject.class);
            }

            // Create the produceFactory() method
            /////////////////////////////////////
            try (MethodCreator produceFactoryMethod = classCreator.getMethodCreator("produceFactory", factoryClassName)) {
                // Add @Produces annotation
                produceFactoryMethod.addAnnotation(Produces.class);
                produceFactoryMethod.addAnnotation(ApplicationScoped.class);

                // Method implementation: return new ThingFactory();
                // Create the method body and return value.
                ResultHandle instance = produceFactoryMethod.newInstance(
                        MethodDescriptor.ofConstructor(factoryClassName)
                );
                produceFactoryMethod.returnValue(instance);
            }

            // Create the produce() method if a script location is configured
            /////////////////////////////////////
            if (scriptLocation != null) {
                try (MethodCreator produceMethod = classCreator.getMethodCreator("produce", proxyClassName)) {
                    // Add @Produces annotation
                    produceMethod.addAnnotation(Produces.class);

                    // Add scope if specified
                    produceMethod.addAnnotation(RequestScoped.class);

                    // Method implementation: return new ThingProxy(scriptLibrary, context);

                    // Get the scriptLibrary static field
                    ResultHandle scriptLibraryHandle = produceMethod.readStaticField(
                            FieldDescriptor.of(producerClassName, "DEFAULT_SCRIPT", String.class));

                    // Get the context instance field
                    ResultHandle contextHandle = produceMethod.readInstanceField(
                            FieldDescriptor.of(producerClassName, "context", contextClassName),
                            produceMethod.getThis()
                    );

                    // Create the method body and return value.
                    ResultHandle instance = produceMethod.newInstance(
                            MethodDescriptor.ofConstructor(proxyClassName, String.class, contextClassName),
                            scriptLibraryHandle,
                            contextHandle
                    );
                    produceMethod.returnValue(instance);
                }
            }
        }
    }
}
