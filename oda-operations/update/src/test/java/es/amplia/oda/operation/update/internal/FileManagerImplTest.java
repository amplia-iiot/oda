package es.amplia.oda.operation.update.internal;

import es.amplia.oda.operation.update.FileManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

// Uses a real temporary folder: mocking File or Files also intercepts the JVM's own file access
// (class loading), which makes results depend on test order
public class FileManagerImplTest {

    private static final String SEARCHED_NAME = "test";

    private final FileManagerImpl testFileManager = new FileManagerImpl();

    @TempDir
    Path tmp;

    @Test
    public void testExist() throws IOException {
        Path existing = Files.createFile(tmp.resolve("existing"));

        assertTrue(testFileManager.exist(existing.toString()));
    }

    @Test
    public void testExistNoFile() {
        assertFalse(testFileManager.exist(tmp.resolve("no-existing").toString()));
    }

    @Test
    public void testCreateDirectory() throws Exception {
        Path newDirectory = tmp.resolve("new");

        testFileManager.createDirectory(newDirectory.toString());

        assertTrue(Files.isDirectory(newDirectory));
    }

    @Test
    public void testCreateDirectoryIOException() {
        String missingParent = tmp.resolve("missing/parent/dir").toString();

        assertThrows(FileManager.FileException.class, () -> testFileManager.createDirectory(missingParent));
    }

    @Test
    public void testCopy() throws Exception {
        Path source = write(tmp.resolve("file.jar"), "content");
        Path target = tmp.resolve("copy.jar");

        String result = testFileManager.copy(source.toString(), target.toString());

        assertEquals(target.toString(), result);
        assertEquals("content", read(target));
    }

    @Test
    public void testCopyToDirectory() throws Exception {
        Path source = write(tmp.resolve("file.jar"), "content");
        Path targetFolder = Files.createDirectory(tmp.resolve("target"));

        String result = testFileManager.copy(source.toString(), targetFolder.toString());

        assertEquals(targetFolder + File.separator + "file.jar", result);
        assertEquals("content", read(targetFolder.resolve("file.jar")));
    }

    @Test
    public void testCopyIOException() {
        String missingSource = tmp.resolve("missing.jar").toString();
        String target = tmp.resolve("copy.jar").toString();

        assertThrows(FileManager.FileException.class, () -> testFileManager.copy(missingSource, target));
    }

    @Test
    public void testDelete() throws Exception {
        Path file = write(tmp.resolve("delete.jar"), "content");

        testFileManager.delete(file.toString());

        assertFalse(Files.exists(file));
    }

    @Test
    public void testDeleteIOException() {
        String missing = tmp.resolve("missing.jar").toString();

        assertThrows(FileManager.FileException.class, () -> testFileManager.delete(missing));
    }

    @Test
    public void testFindHandleListFiles() throws IOException {
        Path found = write(tmp.resolve("bundle-test-1.0.jar"), "");
        write(tmp.resolve("other.jar"), "");

        assertEquals(found.toString(), testFileManager.find(tmp.toString(), SEARCHED_NAME));
    }

    @Test
    public void testFindHandleListFilesNull() {
        assertNull(testFileManager.find(tmp.resolve("not-a-folder").toString(), SEARCHED_NAME));
    }

    @Test
    public void testFindHandleListFilesEmpty() {
        assertNull(testFileManager.find(tmp.toString(), SEARCHED_NAME));
    }

    @Test
    public void testInsertInFile() throws IOException {
        Path file = write(tmp.resolve("insert.txt"), "Above this there should be something");

        String result = testFileManager.insertInFile("Correctly tested\n", 0, file.toString());

        assertEquals(file.toString(), result);
        assertEquals("Correctly tested\nAbove this there should be something", read(file));
    }

    private static Path write(Path path, String content) throws IOException {
        return Files.write(path, content.getBytes(StandardCharsets.UTF_8));
    }

    private static String read(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
}
