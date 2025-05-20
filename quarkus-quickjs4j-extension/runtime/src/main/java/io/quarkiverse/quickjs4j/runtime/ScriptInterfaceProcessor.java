package io.quarkiverse.quickjs4j.runtime;

import io.roastedroot.quickjs4j.annotations.ScriptInterface;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            log(NOTE, "Generating factory class for script interface: " + element, null);
            generateFactoryFor((TypeElement) element);
        }

        return false;
    }

    private void generateFactoryFor(TypeElement element) {
        String packageName = getPackageName(element).toString();
        String scriptInterfaceName = element.getSimpleName().toString();
        String scriptInterfaceFQN = packageName + "." + scriptInterfaceName;
        String factoryClassName = scriptInterfaceName + "Factory";
        String factoryClassFQN = packageName + "." + factoryClassName;
        String proxyClassName = element.getSimpleName().toString() + "_Proxy";
        String proxyClassFQN = packageName + "." + proxyClassName;

        AnnotationMirror scriptInterfaceAnnotation = getScriptInterfaceAnnotation(element);
        Element contextClass = getContextClassFromAnnotation(scriptInterfaceAnnotation);

        String contextClassPackage = getPackageName(contextClass).toString();
        String contextClassName = contextClass.getSimpleName().toString();
        String contextClassFQN = contextClassPackage + "." + contextClassName;

        // Create the factory class and add all imports
        JavaClassSource factorySource = Roaster.create(JavaClassSource.class);
        factorySource.setPackage(packageName);
        factorySource.setName(factoryClassName);
        factorySource.addImport(scriptInterfaceFQN);
        factorySource.addImport(contextClassFQN);
        factorySource.addImport(proxyClassFQN);
        factorySource.addImport(ScriptInterfaceFactory.class);
        factorySource.addImport(Files.class);
        factorySource.addImport(Paths.class);
        factorySource.addImport(Path.class);

        // The factory class implements ScriptInterfaceFactory<T, C>
        factorySource.addInterface(template("INTERFACE<TYPE, CONTEXT>", Map.of(
                "INTERFACE", ScriptInterfaceFactory.class.getSimpleName(),
                "TYPE", scriptInterfaceName,
                "CONTEXT", contextClassName
        )));

        // Create the create(String, Context) method
        String produceMethodBody = """
            return new PROXY_CLASS_NAME(scriptLibrary, context);
        """;
        MethodSource<JavaClassSource> create1MethodSource = factorySource.addMethod();
        create1MethodSource.setPublic();
        create1MethodSource.setReturnType(scriptInterfaceName);
        create1MethodSource.setName("create");
        create1MethodSource.addParameter("String", "scriptLibrary");
        create1MethodSource.addParameter(contextClassName, "context");
        create1MethodSource.setBody(template(produceMethodBody, Map.of(
                "PROXY_CLASS_NAME", proxyClassName
        )));

        // Create the create(Path, Context) method
        String create2MethodBody = """
            Path fullScriptPath = scriptPath;
            if (!fullScriptPath.isAbsolute()) {
                String workingDir = System.getProperty("user.dir");
                fullScriptPath = Paths.get(workingDir).resolve(scriptPath).normalize();
            }
            String scriptLibrary = Files.readString(fullScriptPath);
            return create(scriptLibrary, context);
        """;
        MethodSource<JavaClassSource> create2MethodSource = factorySource.addMethod();
        create2MethodSource.setPublic();
        create2MethodSource.setReturnType(scriptInterfaceName);
        create2MethodSource.setName("create");
        create2MethodSource.addParameter(Path.class.getSimpleName(), "scriptPath");
        create2MethodSource.addParameter(contextClassName, "context");
        create2MethodSource.addThrows(IOException.class);
        create2MethodSource.setBody(create2MethodBody);

        try (Writer writer = filer().createSourceFile(factoryClassFQN, element).openWriter()) {
            writer.write(factorySource.toString());
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
            if (annotationQualifiedName.equals(ScriptInterface.class.getName())) {
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
}