package es.amplia.oda.datastreams.deviceinfofx30;

import es.amplia.oda.core.commons.utils.CommandExecutionException;

import java.io.IOException;

public interface ScriptsLoader extends AutoCloseable {
    void loadScripts(String source, String path) throws CommandExecutionException, IOException;
    @Override
    void close();
}
