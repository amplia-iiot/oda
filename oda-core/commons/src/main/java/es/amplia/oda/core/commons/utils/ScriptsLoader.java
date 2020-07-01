package es.amplia.oda.core.commons.utils;

import java.io.IOException;

public interface ScriptsLoader extends AutoCloseable {
    /**
     * Method use for extract the script of a bundle's jar file and deploy it into the expected directory.
     * @param source path from the bundles jar's are stored.
     * @param path path where the scripts of the jar extracted will be stored.
     * @param artifactId artifact id of the bundle that the loader have to extract.
     * @throws CommandExecutionException Exception that will be sent if a command is executed wrong.
     * @throws IOException Exception that will be sent if the command use an invalid direction.
     */
    void loadScripts(String source, String path, String artifactId) throws CommandExecutionException, IOException;

    /**
     * Method used to close the thread that check and reconnect the GSM connection.
     */
    @Override
    void close();
}
