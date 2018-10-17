package es.amplia.oda.operation.update;

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
}
