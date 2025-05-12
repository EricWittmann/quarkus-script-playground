package io.apicurio.qqi.processor;

import io.apicurio.qqi.annotations.ScriptInterface;
import io.roastedroot.quickjs4j.annotations.Builtins;
import io.roastedroot.quickjs4j.annotations.GuestFunction;
import io.roastedroot.quickjs4j.annotations.HostFunction;
import io.roastedroot.quickjs4j.annotations.Invokables;
import io.roastedroot.quickjs4j.core.Engine;
import io.roastedroot.quickjs4j.core.Runner;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Generated;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

public class ScriptInterfaceProcessor extends AbstractProcessor {

    static PackageElement getPackageName(Element element) {
        Element enclosing = element;
        while (enclosing.getKind() != ElementKind.PACKAGE) {
            enclosing = enclosing.getEnclosingElement();
        }
        return (PackageElement) enclosing;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    Elements elements() {
        return processingEnv.getElementUtils();
    }

    Filer filer() {
        return processingEnv.getFiler();
    }

    void log(Diagnostic.Kind kind, String message, Element element) {
        processingEnv.getMessager().printMessage(kind, message, element);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(ScriptInterface.class.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(ScriptInterface.class)) {
            log(NOTE, "Generating code for script interface: " + element, null);
            generateFor((TypeElement) element);
        }

        return false;
    }

    private void generateFor(TypeElement element) {
        generateGuestFunctionsInterface(element);
        generateHostFunctionsClass(element);
        generateProxy(element);
        generateProducer(element);
    }

    private void generateGuestFunctionsInterface(TypeElement element) {
        String packageName = getPackageName(element).toString();
        String simpleName = element.getSimpleName().toString() + "_GuestFunctions";
        String qualifiedName = packageName + "." + simpleName;

        // Create the interface for the guest functions
        JavaInterfaceSource guestFunctionsInterface = Roaster.create(JavaInterfaceSource.class);
        guestFunctionsInterface.setPackage(packageName);
        guestFunctionsInterface.setName(simpleName);
        guestFunctionsInterface.addImport(Generated.class);
        guestFunctionsInterface
                .addAnnotation()
                .setName(Generated.class.getSimpleName())
                .setStringValue(getClass().getName());

        // Annotate with @Invokables
        guestFunctionsInterface.addImport(Invokables.class);
        AnnotationSource<JavaInterfaceSource> invokableAnnotation = guestFunctionsInterface.addAnnotation();
        invokableAnnotation.setName(Invokables.class.getSimpleName());

        // TODO move somewhere it's needed:
        AnnotationMirror scriptInterfaceAnnotation = getScriptInterfaceAnnotation(element);
        String script = getScriptFromAnnotation(scriptInterfaceAnnotation);
        Element contextClass = getContextClassFromAnnotation(scriptInterfaceAnnotation);

        // Add/copy all methods from the interface to this one
        mirrorAllInterfaceMethods(element, guestFunctionsInterface);

        try (Writer writer = filer().createSourceFile(qualifiedName, element).openWriter()) {
            writer.write(guestFunctionsInterface.toString());
        } catch (IOException e) {
            log(ERROR, format("Failed to create %s file: %s", qualifiedName, e), null);
        }
    }

    private void generateHostFunctionsClass(TypeElement element) {
        AnnotationMirror scriptInterfaceAnnotation = getScriptInterfaceAnnotation(element);
        Element contextClass = getContextClassFromAnnotation(scriptInterfaceAnnotation);

        String packageName = getPackageName(contextClass).toString();
        String simpleName = contextClass.getSimpleName().toString() + "_HostFunctions";
        String contextClassName = contextClass.getSimpleName().toString();
        String contextClassFQName = packageName + "." + contextClassName;
        String qualifiedName = packageName + "." + simpleName;

        // Create the class for the host functions
        JavaClassSource hostFunctionsClass = Roaster.create(JavaClassSource.class);
        hostFunctionsClass.setPackage(packageName);
        hostFunctionsClass.setName(simpleName);
        hostFunctionsClass.addImport(contextClassFQName);
        hostFunctionsClass.addImport(Generated.class);
        hostFunctionsClass
                .addAnnotation()
                .setName(Generated.class.getSimpleName())
                .setStringValue(getClass().getName());

        // Annotate with @Builtins
        hostFunctionsClass.addImport(Builtins.class);
        AnnotationSource<JavaClassSource> builtinsAnnotation = hostFunctionsClass.addAnnotation();
        builtinsAnnotation.setName(Builtins.class.getSimpleName());
        builtinsAnnotation.setStringValue(contextClassName);

        // Create delegate field
        FieldSource<JavaClassSource> delegateField = hostFunctionsClass.addField();
        delegateField.setPrivate();
        delegateField.setFinal(true);
        delegateField.setType(contextClassFQName);
        delegateField.setName("delegate");

        // Create constructor
        MethodSource<JavaClassSource> constructorMethod = hostFunctionsClass.addMethod();
        constructorMethod.setConstructor(true);
        constructorMethod.setPublic();
        constructorMethod.addParameter(contextClassName, "context");
        constructorMethod.setBody("this.delegate = context;");

        delegateAllContextClassMethods((TypeElement) contextClass, hostFunctionsClass);

        try (Writer writer = filer().createSourceFile(qualifiedName, element).openWriter()) {
            writer.write(hostFunctionsClass.toString());
        } catch (IOException e) {
            log(ERROR, format("Failed to create %s file: %s", qualifiedName, e), null);
        }
    }

    private void generateProxy(TypeElement element) {
        String packageName = getPackageName(element).toString();
        String scriptInterfaceName = element.getSimpleName().toString();
        String scriptInterfaceFQN = packageName + "." + scriptInterfaceName;
        String proxyClassName = element.getSimpleName().toString() + "Proxy";
        String proxyClassFQN = packageName + "." + proxyClassName;
        String scriptGuestFunctionsName = scriptInterfaceName + "_GuestFunctions";
        String scriptGuestFunctionsFQN = packageName + "." + scriptGuestFunctionsName;
        String scriptInvokablesName = scriptGuestFunctionsName + "_Invokables";
        String scriptInvokablesFQN = packageName + "." + scriptInvokablesName;

        AnnotationMirror scriptInterfaceAnnotation = getScriptInterfaceAnnotation(element);
        Element contextClass = getContextClassFromAnnotation(scriptInterfaceAnnotation);

        String contextClassPackage = getPackageName(contextClass).toString();
        String contextClassName = contextClass.getSimpleName().toString();
        String contextClassFQName = contextClassPackage + "." + contextClassName;
        String contextClassHostFunctionsName = contextClassName + "_HostFunctions";
        String contextClassHostFunctionsFQN = contextClassPackage + "." + contextClassHostFunctionsName;
        String contextClassBuiltinsName = contextClassHostFunctionsName + "_Builtins";
        String contextClassBuiltinsFQN = contextClassPackage + "." + contextClassBuiltinsName;

        // Create the proxy class and add all imports
        JavaClassSource proxyClass = Roaster.create(JavaClassSource.class);
        proxyClass.setPackage(packageName);
        proxyClass.setName(proxyClassName);
        proxyClass.addImport(scriptInterfaceFQN);
        proxyClass.addImport(scriptInvokablesFQN);
        proxyClass.addImport(scriptGuestFunctionsFQN);
        proxyClass.addImport(contextClassFQName);
        proxyClass.addImport(contextClassHostFunctionsFQN);
        proxyClass.addImport(contextClassBuiltinsFQN);
        proxyClass.addImport(AutoCloseable.class);
        proxyClass.addImport(Engine.class);
        proxyClass.addImport(Runner.class);

        // The class should implement the script interface and Autocloseable
        proxyClass.addInterface(AutoCloseable.class.getSimpleName());
        proxyClass.addInterface(scriptInterfaceName);

        // Create the spi field
        FieldSource<JavaClassSource> spiField = proxyClass.addField();
        spiField.setName("spi");
        spiField.setPrivate();
        spiField.setFinal(true);
        spiField.setType(scriptGuestFunctionsName);

        // Create the runner field
        FieldSource<JavaClassSource> runnerField = proxyClass.addField();
        runnerField.setName("runner");
        runnerField.setPrivate();
        runnerField.setFinal(true);
        runnerField.setType("Runner");

        // Create the constructor
        static final String proxyConstructorBody = """
            HOST_FUNCTIONS_NAME hostFunctions = new HOST_FUNCTIONS_NAME(context);
            Engine engine = Engine.builder()
                    .addBuiltins(BUILTINS_NAME.toBuiltins(hostFunctions))
                    .addInvokables(INVOKABLES_NAME.toInvokables())
                    .build();
            this.runner = Runner.builder().withEngine(engine).build();
            this.spi = INVOKABLES_NAME.create(script, runner);
        """;
        MethodSource<JavaClassSource> constructorSource = proxyClass.addMethod();
        constructorSource.setConstructor(true);
        constructorSource.setPublic();
        constructorSource.addParameter(String.class.getSimpleName(), "script");
        constructorSource.addParameter(contextClassName, "context");
        constructorSource.setBody(template(proxyConstructorBody, Map.of(
                "HOST_FUNCTIONS_NAME", contextClassHostFunctionsName,
                "BUILTINS_NAME", contextClassBuiltinsName,
                "INVOKABLES_NAME", scriptInvokablesName
        )));

        // Create the close() method
        MethodSource<JavaClassSource> closeMethodSource = proxyClass.addMethod();
        closeMethodSource.setName("close");
        closeMethodSource.setPublic();
        closeMethodSource.setBody("runner.close();");
        closeMethodSource.addAnnotation(Override.class.getSimpleName());

        delegateAllScriptInterfaceMethods((TypeElement) element, proxyClass);

        try (Writer writer = filer().createSourceFile(proxyClassFQN, element).openWriter()) {
            writer.write(proxyClass.toString());
        } catch (IOException e) {
            log(ERROR, format("Failed to create %s file: %s", proxyClassFQN, e), null);
        }
    }

    private static String template(String templateSource, Map<String, String> templateParams) {
        String rval = templateSource;
        Set<Map.Entry<String, String>> entries = templateParams.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            String key = entry.getKey();
            String value = entry.getValue();
            rval = rval.replaceAll(key, value);
        }
        return rval;
    }

    private void generateProducer(TypeElement element) {
        String packageName = getPackageName(element).toString();
        String scriptInterfaceName = element.getSimpleName().toString();
        String scriptInterfaceFQN = packageName + "." + scriptInterfaceName;
        String producerName = scriptInterfaceName + "Producer";
        String producerFQN = packageName + "." + producerName;
        String proxyClassName = element.getSimpleName().toString() + "Proxy";
        String proxyClassFQN = packageName + "." + proxyClassName;

        AnnotationMirror scriptInterfaceAnnotation = getScriptInterfaceAnnotation(element);
        String script = getScriptFromAnnotation(scriptInterfaceAnnotation);
        Element contextClass = getContextClassFromAnnotation(scriptInterfaceAnnotation);

        String contextClassPackage = getPackageName(contextClass).toString();
        String contextClassName = contextClass.getSimpleName().toString();
        String contextClassFQName = contextClassPackage + "." + contextClassName;

        // Create the proxy class and add all imports
        JavaClassSource producerSource = Roaster.create(JavaClassSource.class);
        producerSource.setPackage(packageName);
        producerSource.setName(producerName);
        producerSource.addImport(scriptInterfaceFQN);
        producerSource.addImport(contextClassFQName);
        producerSource.addImport(proxyClassFQN);
        producerSource.addImport(ScriptInterface.class);
        producerSource.addImport(ApplicationScoped.class);
        producerSource.addImport(Produces.class);
        producerSource.addImport(Inject.class);
        producerSource.addImport(Files.class);
        producerSource.addImport(Paths.class);
        producerSource.addImport(Path.class);

        // Create the context field
        FieldSource<JavaClassSource> contextField = producerSource.addField();
        contextField.setName("context");
        contextField.setType(contextClassName);
        contextField.addAnnotation(Inject.class);

        // Create the produceXXX() method
        String produceMethodBody = """
            Class<?> theClass = SCRIPT_INTERFACE_NAME.class;
            ScriptInterface scriptInterfaceAnnotation = theClass.getAnnotation(ScriptInterface.class);
            if (scriptInterfaceAnnotation == null) {
                throw new RuntimeException("Missing @ScriptInterface annotation");
            }
    
            String scriptPath = scriptInterfaceAnnotation.script();
            String workingDir = System.getProperty("user.dir");
            Path fullScriptPath = Paths.get(workingDir, scriptPath);
    
            return PRODUCE_METHOD_NAME(fullScriptPath);
        """;
        String produceMethodName = "produce" + scriptInterfaceName;
        MethodSource<JavaClassSource> produceMethodSource = producerSource.addMethod();
        produceMethodSource.setName(produceMethodName);
        produceMethodSource.setPublic();
        produceMethodSource.setReturnType(scriptInterfaceName);
        produceMethodSource.addThrows(Exception.class);
        produceMethodSource.addAnnotation(Produces.class.getSimpleName());
        produceMethodSource.setBody(template(produceMethodBody, Map.of(
                "SCRIPT_INTERFACE_NAME", scriptInterfaceName,
                "PRODUCE_METHOD_NAME", produceMethodName
        )));

        // Create the produceXXX() method
        String produceWithScriptMethodBody = """
            String script = Files.readString(fullScriptPath);
            return new PROXY_CLASS_NAME(script, context);
        """;
        MethodSource<JavaClassSource> produceWithScriptMethodSource = producerSource.addMethod();
        produceWithScriptMethodSource.setName(produceMethodName);
        produceWithScriptMethodSource.setPublic();
        produceWithScriptMethodSource.addParameter(Path.class.getSimpleName(), "fullScriptPath");
        produceWithScriptMethodSource.setReturnType(scriptInterfaceName);
        produceWithScriptMethodSource.addThrows(Exception.class);
        produceWithScriptMethodSource.setBody(template(produceWithScriptMethodBody, Map.of(
                "PROXY_CLASS_NAME", proxyClassName
        )));

        try (Writer writer = filer().createSourceFile(producerFQN, element).openWriter()) {
            writer.write(producerSource.toString());
        } catch (IOException e) {
            log(ERROR, format("Failed to create %s file: %s", proxyClassFQN, e), null);
        }
    }

    private static void delegateAllContextClassMethods(TypeElement contextClass, JavaClassSource hostFunctionsClass) {
        List<? extends Element> enclosedElements = contextClass.getEnclosedElements();

        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement methodElement = (ExecutableElement) enclosedElement;

                String methodName = methodElement.getSimpleName().toString();
                MethodSource<JavaClassSource> methodSource = hostFunctionsClass.addMethod();
                methodSource.setName(methodName);
                methodSource.addAnnotation(HostFunction.class);
                methodSource.setPublic();

                // Method arguments/parameters
                List<? extends VariableElement> parameters = methodElement.getParameters();
                List<String> paramList = new ArrayList<>(parameters.size());
                for (VariableElement param : parameters) {
                    String paramName = param.getSimpleName().toString();
                    TypeMirror paramType = param.asType();
                    String paramTypeName = paramType.toString();

                    // TODO probably need to better handle non primitive types
                    // TODO check for annotations on parameters
                    // List<? extends AnnotationMirror> paramAnnotations = param.getAnnotationMirrors();

                    methodSource.addParameter(paramTypeName, paramName);
                    paramList.add(paramName);
                }

                // Return type
                TypeMirror returnType = methodElement.getReturnType();
                String returnTypeName = returnType.toString();
                methodSource.setReturnType(returnTypeName);

                // Get list of thrown exceptions
                List<? extends TypeMirror> thrownTypes = methodElement.getThrownTypes();
                for (TypeMirror thrownType : thrownTypes) {
                    methodSource.addThrows(thrownType.toString());
                }

                methodSource.setBody(java.lang.String.format("delegate.%s(%s);", methodName, java.lang.String.join(", ", paramList)));
            }
        }
    }

    private static void mirrorAllInterfaceMethods(TypeElement interfaceElement, JavaInterfaceSource guestFunctionsInterface) {
        // Get all enclosed elements (methods, fields, nested types, etc.)
        List<? extends Element> enclosedElements = interfaceElement.getEnclosedElements();

        // Filter for just the methods
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement methodElement = (ExecutableElement) enclosedElement;

                String methodName = methodElement.getSimpleName().toString();
                MethodSource<JavaInterfaceSource> methodSource = guestFunctionsInterface.addMethod();
                methodSource.setName(methodName);
                methodSource.addAnnotation(GuestFunction.class);

                // Method arguments/parameters
                List<? extends VariableElement> parameters = methodElement.getParameters();
                for (VariableElement param : parameters) {
                    String paramName = param.getSimpleName().toString();
                    TypeMirror paramType = param.asType();
                    String paramTypeName = paramType.toString();

                    // TODO check for annotations on parameters
                    // List<? extends AnnotationMirror> paramAnnotations = param.getAnnotationMirrors();

                    methodSource.addParameter(paramTypeName, paramName);
                }

                // Return type
                TypeMirror returnType = methodElement.getReturnType();
                String returnTypeName = returnType.toString();
                methodSource.setReturnType(returnTypeName);

                // Get list of thrown exceptions
                List<? extends TypeMirror> thrownTypes = methodElement.getThrownTypes();
                for (TypeMirror thrownType : thrownTypes) {
                    methodSource.addThrows(thrownType.toString());
                }
            }
        }
    }

    private static void delegateAllScriptInterfaceMethods(TypeElement interfaceElement, JavaClassSource proxyClassSource) {
        // Get all enclosed elements (methods, fields, nested types, etc.)
        List<? extends Element> enclosedElements = interfaceElement.getEnclosedElements();

        // Filter for just the methods
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.METHOD) {
                ExecutableElement methodElement = (ExecutableElement) enclosedElement;

                String methodName = methodElement.getSimpleName().toString();
                MethodSource<JavaClassSource> methodSource = proxyClassSource.addMethod();
                methodSource.setPublic();
                methodSource.setName(methodName);
                methodSource.addAnnotation(Override.class);

                // Method arguments/parameters
                List<? extends VariableElement> parameters = methodElement.getParameters();
                List<String> paramNames = new ArrayList<>(parameters.size());
                for (VariableElement param : parameters) {
                    String paramName = param.getSimpleName().toString();
                    TypeMirror paramType = param.asType();
                    String paramTypeName = paramType.toString();

                    // TODO check for annotations on parameters
                    // List<? extends AnnotationMirror> paramAnnotations = param.getAnnotationMirrors();

                    methodSource.addParameter(paramTypeName, paramName);
                    paramNames.add(paramName);
                }

                // Return type
                TypeMirror returnType = methodElement.getReturnType();
                String returnTypeName = returnType.toString();
                methodSource.setReturnType(returnTypeName);

                // Get list of thrown exceptions
                List<? extends TypeMirror> thrownTypes = methodElement.getThrownTypes();
                for (TypeMirror thrownType : thrownTypes) {
                    methodSource.addThrows(thrownType.toString());
                }

                methodSource.setBody(template("return spi.METHOD_NAME(PARAMS);", Map.of(
                        "METHOD_NAME", methodName,
                        "PARAMS", java.lang.String.join(", ", paramNames)
                )));
            }
        }
    }

    private static AnnotationMirror getScriptInterfaceAnnotation(TypeElement element) {
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Element annotationElement = annotationType.asElement();
            String annotationQualifiedName = getPackageName(annotationElement).toString() + "." + annotationElement.getSimpleName();
            if (annotationQualifiedName.equals(ScriptInterface.class.getName())) {
                return annotationMirror;
            }
        }
        return null;
    }

    private static String getScriptFromAnnotation(AnnotationMirror scriptInterfaceAnnotation) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> values = scriptInterfaceAnnotation.getElementValues();
        Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> entries = values.entrySet();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : entries) {
            String name = entry.getKey().getSimpleName().toString();
            if (name.equals("script")) {
                return entry.getValue().getValue().toString();
            }
        }
        return "";
    }

    private static Element getContextClassFromAnnotation(AnnotationMirror scriptInterfaceAnnotation) {
        Map<? extends ExecutableElement, ? extends AnnotationValue> values = scriptInterfaceAnnotation.getElementValues();
        Set<? extends Map.Entry<? extends ExecutableElement, ? extends AnnotationValue>> entries = values.entrySet();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : entries) {
            String name = entry.getKey().getSimpleName().toString();
            if (name.equals("context")) {
                AnnotationValue value = entry.getValue();
                TypeMirror typeMirror = (TypeMirror) value.getValue();
                if (typeMirror.getKind() == TypeKind.DECLARED) {
                    DeclaredType declaredType = (DeclaredType) typeMirror;
                    TypeElement typeElement = (TypeElement) declaredType.asElement();
                    return typeElement;
                }
            }
        }
        return null;
    }
}
