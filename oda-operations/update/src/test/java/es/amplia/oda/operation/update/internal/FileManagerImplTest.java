package es.amplia.oda.operation.update.internal;

import es.amplia.oda.operation.update.FileManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ FileManagerImpl.class, Files.class })
public class FileManagerImplTest {

    private static final String DIRECTORY_TO_SEARCH = "directory/to/search";
    private static final String SEARCHED_NAME = "test";
    private final FileManagerImpl testFileManager = new FileManagerImpl();

    @Mock
    private File mockedFile;
    @Mock
    private File mockedFile2;
    @Mock
    private Path mockedPath;
    @Mock
    private Path mockedPath2;

    @Test
    public void testExist() throws Exception {
        String existingTest = "existing/test";

        PowerMockito.whenNew(File.class).withArguments(existingTest).thenReturn(mockedFile);
        when(mockedFile.exists()).thenReturn(true);

        assertTrue(testFileManager.exist(existingTest));
    }

    public void testExistNoFile() throws Exception {
        String noExistingTest = "no/existing/test";

        PowerMockito.whenNew(File.class).withArguments(noExistingTest).thenReturn(mockedFile);
        when(mockedFile.exists()).thenReturn(false);

        assertFalse(testFileManager.exist(noExistingTest));
    }

    @Test
    public void testCreateDirectory() throws Exception {
        String newDirectory = "new/directory";

        PowerMockito.whenNew(File.class).withArguments(newDirectory).thenReturn(mockedFile);
        when(mockedFile.toPath()).thenReturn(mockedPath);
        PowerMockito.mockStatic(Files.class);

        testFileManager.createDirectory(newDirectory);

        PowerMockito.verifyStatic(Files.class);
        Files.createDirectory(eq(mockedPath));
    }

    @Test(expected = FileManager.FileException.class)
    public void testCreateDirectoryIOException() throws Exception {
        String newDirectory = "new/directory";

        PowerMockito.whenNew(File.class).withArguments(newDirectory).thenReturn(mockedFile);
        when(mockedFile.toPath()).thenReturn(mockedPath);
        PowerMockito.mockStatic(Files.class);
        PowerMockito.when(Files.createDirectory(eq(mockedPath))).thenThrow(new IOException(""));

        testFileManager.createDirectory(newDirectory);

        fail("File Exception must be thrown");
    }

    @Test
    public void testCopy() throws Exception {
        String sourceFile = "test/source/file.jar";
        String targetFile = "test/target/file.jar";

        PowerMockito.whenNew(File.class).withArguments(sourceFile).thenReturn(mockedFile);
        PowerMockito.whenNew(File.class).withArguments(targetFile).thenReturn(mockedFile2);
        when(mockedFile.toPath()).thenReturn(mockedPath);
        when(mockedFile2.toPath()).thenReturn(mockedPath2);
        when(mockedFile2.getPath()).thenReturn(targetFile);
        PowerMockito.mockStatic(Files.class);

        String result = testFileManager.copy(sourceFile, targetFile);

        assertEquals(targetFile, result);
        PowerMockito.verifyStatic(Files.class);
        Files.copy(eq(mockedPath), eq(mockedPath2));
    }

    @Test
    public void testCopyToDirectory() throws Exception {
        String sourceFilename = "file.jar";
        String sourceFile = "test/source/" + sourceFilename;
        String targetFolder = "test/target/";
        File mockedFinalTargetFile = mock(File.class);
        Path mockedFinalTargetFilePath = mock(Path.class);
        String resultPath = targetFolder + sourceFilename;

        PowerMockito.whenNew(File.class).withArguments(sourceFile).thenReturn(mockedFile);
        PowerMockito.whenNew(File.class).withArguments(targetFolder).thenReturn(mockedFile2);
        when(mockedFile2.isDirectory()).thenReturn(true);
        when(mockedFile.getName()).thenReturn(sourceFilename);
        PowerMockito.whenNew(File.class).withArguments(mockedFile2, sourceFilename).thenReturn(mockedFinalTargetFile);
        when(mockedFile.toPath()).thenReturn(mockedPath);
        when(mockedFinalTargetFile.toPath()).thenReturn(mockedFinalTargetFilePath);
        when(mockedFinalTargetFile.getPath()).thenReturn(resultPath);
        PowerMockito.mockStatic(Files.class);

        String result = testFileManager.copy(sourceFile, targetFolder);

        assertEquals(targetFolder + sourceFilename, result);
        PowerMockito.verifyStatic(Files.class);
        Files.copy(eq(mockedPath), eq(mockedFinalTargetFilePath));
    }

    @Test(expected = FileManager.FileException.class)
    public void testCopyIOException() throws Exception {
        String sourceFile = "test/source/file.jar";
        String targetFile = "test/target/file.jar";

        PowerMockito.whenNew(File.class).withArguments(sourceFile).thenReturn(mockedFile);
        PowerMockito.whenNew(File.class).withArguments(targetFile).thenReturn(mockedFile2);
        when(mockedFile.toPath()).thenReturn(mockedPath);
        when(mockedFile2.toPath()).thenReturn(mockedPath2);
        PowerMockito.mockStatic(Files.class);
        PowerMockito.when(Files.copy(eq(mockedPath), eq(mockedPath2))).thenThrow(new IOException(""));

        testFileManager.copy(sourceFile, targetFile);

        fail("File Exception must be thrown");
    }

    @Test
    public void testDelete() throws Exception {
        String deleteFile = "file/to/delete.jar";

        PowerMockito.whenNew(File.class).withArguments(deleteFile).thenReturn(mockedFile);
        when(mockedFile.toPath()).thenReturn(mockedPath);
        PowerMockito.mockStatic(Files.class);

        testFileManager.delete(deleteFile);
        PowerMockito.verifyStatic(Files.class);
        Files.delete(eq(mockedPath));
    }

    @Test(expected = FileManager.FileException.class)
    public void testDeleteIOException() throws Exception {
        String deleteFile = "file/to/delete.jar";

        PowerMockito.whenNew(File.class).withArguments(deleteFile).thenReturn(mockedFile);
        when(mockedFile.toPath()).thenReturn(mockedPath);
        PowerMockito.mockStatic(Files.class);
        PowerMockito.doThrow(new IOException("")).when(Files.class);
        Files.delete(eq(mockedPath));

        testFileManager.delete(deleteFile);
        PowerMockito.verifyStatic(Files.class);
        Files.delete(eq(mockedPath));
    }

    @Test
    public void testFindHandleListFiles() throws Exception {
        File mockedFindFile1 = mock(File.class);
        File mockedFindFile2 = mock(File.class);
        String expectedResult = "directory/to/search/test-1.0.0.jar";

        PowerMockito.whenNew(File.class).withArguments(DIRECTORY_TO_SEARCH).thenReturn(mockedFile);
        when(mockedFile.listFiles(any(FilenameFilter.class))).thenReturn(new File[] {mockedFindFile1, mockedFindFile2});
        when(mockedFindFile1.getPath()).thenReturn(expectedResult);

        String result = testFileManager.find(DIRECTORY_TO_SEARCH, SEARCHED_NAME);

        assertEquals(expectedResult, result);
    }

    @Test
    public void testFindHandleListFilesNull() throws Exception {
        PowerMockito.whenNew(File.class).withArguments(DIRECTORY_TO_SEARCH).thenReturn(mockedFile);
        when(mockedFile.listFiles(any(FilenameFilter.class))).thenReturn(null);

        String searchedFile = testFileManager.find(DIRECTORY_TO_SEARCH, SEARCHED_NAME);

        assertNull(searchedFile);
    }

    @Test
    public void testFindHandleListFilesEmpty() throws Exception {
        String searchedName = "test";

        PowerMockito.whenNew(File.class).withArguments(DIRECTORY_TO_SEARCH).thenReturn(mockedFile);
        when(mockedFile.listFiles(any(FilenameFilter.class))).thenReturn(new File[]{});

        String searchedFile = testFileManager.find(DIRECTORY_TO_SEARCH, searchedName);

        assertNull(searchedFile);
    }

    @Test
    public void testInsertInFile() throws IOException {
        File fileTest = new File("test.txt");
        fileTest.createNewFile();
        FileWriter fileWriterTest = new FileWriter(fileTest);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriterTest);
        bufferedWriter.write("Above this there should be something");
        bufferedWriter.flush();
        bufferedWriter.close();

        testFileManager.insertInFile("Correctly tested\n", 0, fileTest.getPath());

        FileReader fileReader = new FileReader(fileTest);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder builder = new StringBuilder();
        String line = "";

        while ((line = bufferedReader.readLine()) != null) {
            builder.append(line + "\n");
        }
        builder.deleteCharAt(builder.length() - 1);
        fileTest.delete();
        assertEquals("Correctly tested\n" + "Above this there should be something", builder.toString());
    }
}