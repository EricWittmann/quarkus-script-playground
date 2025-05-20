package io.quarkiverse.quickjs4j.runtime;

import java.io.IOException;
import java.nio.file.Path;

public interface ScriptInterfaceFactory<T, C> {

    T create(String scriptLibrary, C context);

    T create(Path scriptPath, C context) throws IOException;

}
