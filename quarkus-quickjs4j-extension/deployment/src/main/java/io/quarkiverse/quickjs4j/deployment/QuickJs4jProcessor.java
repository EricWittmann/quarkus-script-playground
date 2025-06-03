package io.quarkiverse.quickjs4j.deployment;

import io.quarkiverse.quickjs4j.ScriptInterfaceFactory;
import io.quarkiverse.quickjs4j.annotations.ScriptImplementation;
import io.quarkiverse.quickjs4j.runtime.FactoryRecorder;
import io.quarkus.arc.deployment.GeneratedBeanBuildItem;
import io.quarkus.arc.deployment.GeneratedBeanGizmoAdaptor;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
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
import io.quarkus.gizmo.SignatureBuilder;
import io.quarkus.gizmo.Type;
import io.roastedroot.quickjs4j.annotations.ScriptInterface;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import java.util.Collection;

public class QuickJs4jProcessor {

    private static final String FEATURE = "quickjs4j-cdi";
    private static final Logger LOGGER = Logger.getLogger(QuickJs4jProcessor.class);

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    public void build(BuildProducer<AdditionalIndexedClassesBuildItem> producer) {
        producer.produce(new AdditionalIndexedClassesBuildItem(ScriptInterfaceFactory.class.getName()));
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void generateBeans(CombinedIndexBuildItem indexBuildItem, FactoryRecorder factoryRecorder,
            BuildProducer<GeneratedBeanBuildItem> generatedBeans,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeans) {
        LOGGER.info("Generating quickjs4j beans.");

        IndexView index = indexBuildItem.getIndex();
        Collection<AnnotationInstance> targetAnnotations = index.getAnnotations(ScriptInterface.class);

        for (AnnotationInstance annotation : targetAnnotations) {
            if (annotation.target().kind() == org.jboss.jandex.AnnotationTarget.Kind.CLASS) {
                String targetClassName = annotation.target().asClass().name().toString();

                LOGGER.info("Generating quickjs4j beans for: " + targetClassName);

                String proxyClassName = targetClassName + "_Proxy";
                String factoryClassName = targetClassName + "Factory";
                String producerClassName = targetClassName + "Producer";

                ClassType contextClass = getContextClass(annotation);
                String scriptLocation = getScriptLocation(annotation.target());

                boolean hasContext = contextClass != null;
                String contextClassName = hasContext ? contextClass.name().toString() : Void.class.getName();

                // Generate the factory class
                generateFactoryClass(generatedBeans, proxyClassName, factoryClassName, contextClass);

                // Register factory class
                syntheticBeans.produce(SyntheticBeanBuildItem.configure(ScriptInterfaceFactory.class)
                            .unremovable()
                            .scope(ApplicationScoped.class)
                            .setRuntimeInit()
                            .types(
                                    org.jboss.jandex.Type.create(DotName.createSimple(targetClassName), org.jboss.jandex.Type.Kind.CLASS),
                                    org.jboss.jandex.Type.create(DotName.createSimple(contextClassName), org.jboss.jandex.Type.Kind.CLASS)
                            )
                            .setRuntimeInit()
                            .supplier(factoryRecorder.createFactorySupplier(factoryClassName))
                            .done()
                );

                // Generate the producer class
                generateProducerClass(generatedBeans, proxyClassName, producerClassName, factoryClassName,
                        contextClass, scriptLocation);
            }
        }
    }

    private ClassType getContextClass(AnnotationInstance annotation) {
        AnnotationValue value = annotation.value("context");
        if (value != null) {
            return (ClassType) value.asClass();
        }
        return null;
    }

    private String getScriptLocation(AnnotationTarget target) {
        AnnotationInstance scriptAnnotation = target.annotation(ScriptImplementation.class);
        if (scriptAnnotation != null) {
            AnnotationValue value = scriptAnnotation.value("location");
            return value.asString();
        }
        return null;
    }

    private void generateFactoryClass(BuildProducer<GeneratedBeanBuildItem> generatedBeans,
                                      String proxyClassName, String factoryClassName, ClassType contextClassType) {
        boolean hasContext = contextClassType != null;
        String contextClassName = hasContext ? contextClassType.name().toString() : Void.class.getName();

        ClassOutput classOutput = new GeneratedBeanGizmoAdaptor(generatedBeans);

        Type.ParameterizedType factoryClassParameterizedType = Type.ClassType.parameterizedType(
                Type.ClassType.classType(ScriptInterfaceFactory.class),
                Type.ClassType.classType(String.class),
                Type.ClassType.classType(contextClassName)
        );


        try (ClassCreator classCreator = ClassCreator.builder()
                .classOutput(classOutput)
                .className(factoryClassName)
                .interfaces(ScriptInterfaceFactory.class)
                .build()) {

            classCreator.addAnnotation(ApplicationScoped.class);

            // Set class signature with generics
            SignatureBuilder.ClassSignatureBuilder classSignature = SignatureBuilder.forClass();
            classSignature.addInterface(factoryClassParameterizedType);
            classCreator.setSignature(classSignature.build());

            // Create the create(String scriptLibrary, C context) method
            /////////////////////////////////////
            try (MethodCreator createMethod = classCreator.getMethodCreator("create", proxyClassName, String.class, contextClassName)) {
                // Set method signature
                SignatureBuilder.MethodSignatureBuilder methodSignature = SignatureBuilder.forMethod()
                        .setReturnType(Type.ClassType.classType(proxyClassName))
                        .addParameterType(Type.ClassType.classType(String.class))
                        .addParameterType(Type.ClassType.classType(contextClassName));
                createMethod.setSignature(methodSignature.build());

                // Method implementation: return new Thing_Proxy(scriptLibrary, context);
                ResultHandle scriptLibraryParam = createMethod.getMethodParam(0);
                ResultHandle contextParam = createMethod.getMethodParam(1);

                // Create and return Thing_Proxy instance
                ResultHandle proxyInstance = hasContext ? createMethod.newInstance(
                        MethodDescriptor.ofConstructor(
                                proxyClassName,
                                String.class,
                                contextClassName
                        ),
                        scriptLibraryParam,
                        contextParam
                ) : createMethod.newInstance(
                        MethodDescriptor.ofConstructor(
                                proxyClassName,
                                String.class
                        ),
                        scriptLibraryParam
                );

                createMethod.returnValue(proxyInstance);
            }
        }
    }

    private void generateProducerClass(BuildProducer<GeneratedBeanBuildItem> generatedBeans,
                                       String proxyClassName, String producerClassName,
                                       String factoryClassName, ClassType contextClassType, String scriptLocation) {
        boolean hasContext = contextClassType != null;
        boolean hasScriptLocation = scriptLocation != null;

        String contextClassName = hasContext ? contextClassType.name().toString() : Void.class.getName();
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

            // Inject the context - only needed if a script location is configured and a context is configured
            if (hasContext && hasScriptLocation) {
                FieldCreator contextField = classCreator.getFieldCreator("context", contextClassName);
                contextField.setModifiers(0x1); // public
                contextField.addAnnotation(Inject.class);
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

                    ResultHandle instance;
                    if (hasContext) {
                        // Get the context instance field
                        ResultHandle contextHandle = produceMethod.readInstanceField(
                                FieldDescriptor.of(producerClassName, "context", contextClassName),
                                produceMethod.getThis()
                        );

                        // Create the method body and return value.
                        instance = produceMethod.newInstance(
                                MethodDescriptor.ofConstructor(proxyClassName, String.class, contextClassName),
                                scriptLibraryHandle,
                                contextHandle
                        );
                    } else {
                        // Create the method body and return value.
                        instance = produceMethod.newInstance(
                                MethodDescriptor.ofConstructor(proxyClassName, String.class),
                                scriptLibraryHandle
                        );
                    }
                    produceMethod.returnValue(instance);
                }
            }
        }
    }
}
