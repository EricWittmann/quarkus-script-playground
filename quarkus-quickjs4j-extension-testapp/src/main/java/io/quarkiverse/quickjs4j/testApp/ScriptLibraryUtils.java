package io.quarkiverse.quickjs4j.testApp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ScriptLibraryUtils {

    public static String loadScriptLibrary(Path scriptPath) {
        try {
            Path fullScriptPath = scriptPath;
            if (!fullScriptPath.isAbsolute()) {
                String workingDir = System.getProperty("user.dir");
                fullScriptPath = Paths.get(workingDir).resolve(scriptPath).normalize();
            }
            String scriptLibrary = Files.readString(fullScriptPath);
            return scriptLibrary;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String loadScriptLibrary(String scriptPath) {
        return loadScriptLibrary(Paths.get(scriptPath));
    }
}
