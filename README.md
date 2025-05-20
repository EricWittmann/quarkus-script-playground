# quarkus-script-playground

## Iteration #2 : Quarkus Integration via new QuickJS4J Quarkus Extension

### How to run/test

```bash
echo "Build the quickjs4j Quarkus extension"
cd quarkus-quickjs4j-extension
mvn clean install

echo "Build & test the test application"
cd ../quarkus-quickjs4j-extension-testapp
mvn clean package
```

### What's going on?
The extension project contains a new annotation `@ScriptImplementation` and
an annotation processor that generates quickjs4j annotated source code.
For more information on the Quarkus developer experience, see the 
`io.quarkiverse.quickjs4j.testApp.context.ContextTestScript` interface 
in the quarkus-quickjs4j-extension-testapp project.

This integration uses an annotation processor to generate a Factory for
each script interface.  Then it uses Quarkus bytecode generation to generate
a CDI producer class capable of injecting an instance of the script interface
**or** a Factory capable of creating script interface instances.

## Iteration #1 : Quarkus Integration POC using annotation processing

### How to run

```bash
echo "Build the quickjs4j + Quarkus integration (annotation processor)"
cd quarkus-quickjs4j-integration
mvn clean install

echo "Build the test application"
cd ../quarkus-quickjs4j-integration-testapp
mvn clean package
cd ./src/main/ts
npm install
npm run build
cd ../../..
mvn quarkus:dev
```

Once this is running, point your browser to:

http://localhost:8080/test/single
http://localhost:8080/test/multi

### What's going on?
The integration project contains a new annotation `@ScriptInterface` and
an annotation processor that generates quickjs4j annotated source code.
For more information on the Quarkus developer experience, see the 
`io.apicurio.calculator.Calculator` interface in the quarkus-quickjs4j-integration-testapp
project.

