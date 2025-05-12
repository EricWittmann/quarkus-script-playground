# quarkus-script-playground

## How to run

```bash
echo "Build the quickjs4j + Quarkus integration (annotation processor)"
cd quarkus-quickjs4j-integration
mvn clean install

echo "Build the test application"
cd ../quarkus-test-application
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

## What's going on?
The integration project contains a new annotation `@ScriptInterface` and
an annotation processor that generates quickjs4j annotated source code.
For more information on the Quarkus developer experience, see the 
`io.apicurio.calculator.Calculator` interface in the quarkus-test-application
project.