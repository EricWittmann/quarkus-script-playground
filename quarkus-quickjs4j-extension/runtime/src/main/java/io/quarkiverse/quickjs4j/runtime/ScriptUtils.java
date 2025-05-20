package io.quarkiverse.quickjs4j.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class ScriptUtils {

    public static final String loadScriptLibrary(String scriptLocation) {
        Path path = Paths.get(scriptLocation);
        if (!path.isAbsolute()) {
            String workingDir = System.getProperty("user.dir");
            path = Paths.get(workingDir).resolve(path).normalize();
        }
        if (Files.exists(path)) {
            return loadScriptLibrary(path);
        }
        if (isValidURL(scriptLocation)) {
            try {
                URL url = new URL(scriptLocation);
                return loadScriptLibrary(url);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static final String loadScriptLibrary(URL scriptLocation) {
        try {
            URLConnection connection = scriptLocation.openConnection();

            // Read from the input stream
            try (InputStream inputStream = connection.getInputStream();
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            } finally {
                // Close connection if it's HTTP
                if (connection instanceof HttpURLConnection) {
                    ((HttpURLConnection) connection).disconnect();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final String loadScriptLibrary(Path scriptLocation) {
        try {
            return Files.readString(scriptLocation);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static boolean isValidURL(String urlString) {
        if (urlString == null || urlString.isEmpty()) {
            return false;
        }

        try {
            new URL(urlString).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }
}
