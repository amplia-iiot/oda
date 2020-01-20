package es.amplia.oda.operation.update;

import java.io.IOException;

public interface FileManager {
    class FileException extends Exception {
        public FileException(String message) {
            super(message);
        }
    }

    boolean exist(String filePath);

    void createDirectory(String directoryPath) throws FileException;

    String copy(String sourcePath, String targetPath) throws FileException;

    void delete(String filePath) throws FileException;

    String find(String path, String name);

    String insertInFile(String insertedText, int position, String path) throws IOException;
}
