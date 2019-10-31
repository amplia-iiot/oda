package es.amplia.oda.hardware.comms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

class ResourceManagerImpl implements ResourceManager {

    @Override
    public String getResourcePath(String resourceName) {
        try {
            File tmp = File.createTempFile(resourceName, null);
            tmp.deleteOnExit();
            if (!tmp.setExecutable(true)) {
                throw new IllegalArgumentException("Error loading resource " +resourceName);
            }
            loadResourceInFile(resourceName, tmp);
            return tmp.getAbsolutePath();
        } catch (IOException e) {
            throw new IllegalArgumentException("Resource not found " + resourceName, e);
        }
    }

    private void loadResourceInFile(String resourceName, File tmp) {
        try (InputStream inputStream = getClass().getResourceAsStream(resourceName);
             FileOutputStream out = new FileOutputStream(tmp)) {
            byte[] buffer = new byte[inputStream.available()];
            int read = inputStream.read(buffer);
            if (read <= 0) {
                throw new IllegalArgumentException("Error reading resource " + resourceName);
            }
            out.write(buffer);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error loading resource " + resourceName, e);
        }
    }
}
