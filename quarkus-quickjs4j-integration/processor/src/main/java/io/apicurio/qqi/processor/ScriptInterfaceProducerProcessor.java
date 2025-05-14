package io.apicurio.qqi.processor;

import io.apicurio.qqi.annotations.ScriptInterfaceProducer;
import io.roastedroot.quickjs4j.annotations.GuestFunction;
import io.roastedroot.quickjs4j.annotations.HostFunction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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

public class ScriptInterfaceProducerProcessor extends AbstractProcessor {

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
        return Set.of(ScriptInterfaceProducer.class.getName());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(ScriptInterfaceProducer.class)) {
            log(NOTE, "Generating code for script interface: " + element, null);
            generateFor((TypeElement) element);
        }

        return false;
    }

    private void generateFor(TypeElement element) {
        generateProducer(element);
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
        String proxyClassName = element.getSimpleName().toString() + "_Proxy";
        String proxyClassFQN = packageName + "." + proxyClassName;

        AnnotationMirror scriptInterfaceAnnotation = getScriptInterfaceAnnotation(element);
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
        producerSource.addImport(ScriptInterfaceProducer.class);
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
            ScriptInterfaceProducer scriptInterfaceAnnotation = theClass.getAnnotation(ScriptInterfaceProducer.class);
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

    private static AnnotationMirror getScriptInterfaceAnnotation(TypeElement element) {
        List<? extends AnnotationMirror> annotationMirrors = element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            DeclaredType annotationType = annotationMirror.getAnnotationType();
            Element annotationElement = annotationType.asElement();
            String annotationQualifiedName = getPackageName(annotationElement).toString() + "." + annotationElement.getSimpleName();
            if (annotationQualifiedName.equals(ScriptInterfaceProducer.class.getName())) {
                return annotationMirror;
            }
        }
        return null;
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
