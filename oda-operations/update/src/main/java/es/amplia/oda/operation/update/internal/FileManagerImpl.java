package es.amplia.oda.operation.update.internal;

import es.amplia.oda.operation.update.FileManager;

import java.io.*;
import java.nio.file.Files;

public class FileManagerImpl implements FileManager {

    @Override
    public boolean exist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    @Override
    public void createDirectory(String directoryPath) throws FileException {
        File directory = new File(directoryPath);
        try {
            Files.createDirectory(directory.toPath());
        } catch (IOException exception) {
            throw new FileException(
                    String.format("Can not create directory %s: %s", directoryPath, exception.getMessage()));
        }
    }

    @Override
    public String copy(String sourcePath, String targetPath) throws FileException {
        File source = new File(sourcePath);
        File target = getTargetFile(targetPath, source);

        try {
            Files.copy(source.toPath(), target.toPath());
            return target.getPath();
        } catch (IOException exception) {
            throw new FileException(
                    String.format("Can not move %s to %s: %s", sourcePath, targetPath, exception.getMessage()));
        }
    }

    private File getTargetFile(String targetPath, File source) {
        File target = new File(targetPath);
        if (target.isDirectory()) {
            target = new File(target, source.getName());
        }
        return target;
    }

    @Override
    public void delete(String filePath) throws FileException {
        File file = new File(filePath);

        try {
            Files.delete(file.toPath());
        } catch (IOException exception) {
            throw new FileException(
                    String.format("Can not delete %s: %s", filePath, exception.getMessage()));
        }
    }

    public String find(String path, String name) {
        File pathFolder = new File(path);

        FilenameFilter filenameFilter = (dir, filename) -> filename.contains(name);
        File[] oldFilenameVersions = pathFolder.listFiles(filenameFilter);

        if (oldFilenameVersions == null || oldFilenameVersions.length == 0)
            return null;

        return oldFilenameVersions[0].getPath();
    }

    @Override
    public String insertInFile(String insertedText, int position, String path) throws IOException {
        File insertedFile = new File(path);
        FileReader fr = new FileReader(insertedFile);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder result = new StringBuilder();
        String line = "";

        while ((line = br.readLine()) != null) {
            result.append(line + "\n");
        }
        result.deleteCharAt(result.length() - 1);

        result.insert(position, insertedText);

        FileWriter fw = new FileWriter(insertedFile);
        BufferedWriter bw = new BufferedWriter(fw);

        bw.write(result.toString());
        bw.flush();

        bw.close();
        br.close();
        fw.close();
        fr.close();

        return insertedFile.getPath();
    }
}
