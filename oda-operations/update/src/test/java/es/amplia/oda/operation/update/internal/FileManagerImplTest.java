package es.amplia.oda.operation.update.internal;

import es.amplia.oda.operation.update.FileManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class FileManagerImplTest {

    private static final String DIRECTORY_TO_SEARCH = "directory/to/search";
    private static final String SEARCHED_NAME = "test";
    private final FileManagerImpl testFileManager = new FileManagerImpl();

    @Mock
    private Path mockedPath;
    @Mock
    private Path mockedPath2;

    @Test
    public void testExist() throws Exception {
        String existingTest = "existing/test";

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                (mock, mctx) -> when(mock.exists()).thenReturn(true))) {
            assertTrue(testFileManager.exist(existingTest));
        }
    }

    public void testExistNoFile() throws Exception {
        String noExistingTest = "no/existing/test";

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                (mock, mctx) -> when(mock.exists()).thenReturn(false))) {
            assertFalse(testFileManager.exist(noExistingTest));
        }
    }

    @Test
    public void testCreateDirectory() throws Exception {
        String newDirectory = "new/directory";

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                     (mock, mctx) -> when(mock.toPath()).thenReturn(mockedPath));
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            testFileManager.createDirectory(newDirectory);

            mockedFiles.verify(() -> Files.createDirectory(eq(mockedPath)));
        }
    }

    @Test(expected = FileManager.FileException.class)
    public void testCreateDirectoryIOException() throws Exception {
        String newDirectory = "new/directory";

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                     (mock, mctx) -> when(mock.toPath()).thenReturn(mockedPath));
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectory(eq(mockedPath))).thenThrow(new IOException(""));

            testFileManager.createDirectory(newDirectory);

            fail("File Exception must be thrown");
        }
    }

    @Test
    public void testCopy() throws Exception {
        String sourceFile = "test/source/file.jar";
        String targetFile = "test/target/file.jar";

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                     (mock, mctx) -> {
                         if (sourceFile.equals(mctx.arguments().get(0))) {
                             when(mock.toPath()).thenReturn(mockedPath);
                         } else {
                             when(mock.toPath()).thenReturn(mockedPath2);
                             when(mock.getPath()).thenReturn(targetFile);
                         }
                     });
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            String result = testFileManager.copy(sourceFile, targetFile);

            assertEquals(targetFile, result);
            mockedFiles.verify(() -> Files.copy(eq(mockedPath), eq(mockedPath2)));
        }
    }

    @Test
    public void testCopyToDirectory() throws Exception {
        String sourceFilename = "file.jar";
        String sourceFile = "test/source/" + sourceFilename;
        String targetFolder = "test/target/";
        Path mockedFinalTargetFilePath = mock(Path.class);
        String resultPath = targetFolder + sourceFilename;

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                     (mock, mctx) -> {
                         if (mctx.arguments().size() == 2) {
                             when(mock.toPath()).thenReturn(mockedFinalTargetFilePath);
                             when(mock.getPath()).thenReturn(resultPath);
                         } else if (sourceFile.equals(mctx.arguments().get(0))) {
                             when(mock.toPath()).thenReturn(mockedPath);
                             when(mock.getName()).thenReturn(sourceFilename);
                         } else {
                             when(mock.isDirectory()).thenReturn(true);
                         }
                     });
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            String result = testFileManager.copy(sourceFile, targetFolder);

            assertEquals(targetFolder + sourceFilename, result);
            mockedFiles.verify(() -> Files.copy(eq(mockedPath), eq(mockedFinalTargetFilePath)));
        }
    }

    @Test(expected = FileManager.FileException.class)
    public void testCopyIOException() throws Exception {
        String sourceFile = "test/source/file.jar";
        String targetFile = "test/target/file.jar";

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                     (mock, mctx) -> {
                         if (sourceFile.equals(mctx.arguments().get(0))) {
                             when(mock.toPath()).thenReturn(mockedPath);
                         } else {
                             when(mock.toPath()).thenReturn(mockedPath2);
                         }
                     });
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.copy(eq(mockedPath), eq(mockedPath2))).thenThrow(new IOException(""));

            testFileManager.copy(sourceFile, targetFile);

            fail("File Exception must be thrown");
        }
    }

    @Test
    public void testDelete() throws Exception {
        String deleteFile = "file/to/delete.jar";

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                     (mock, mctx) -> when(mock.toPath()).thenReturn(mockedPath));
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            testFileManager.delete(deleteFile);

            mockedFiles.verify(() -> Files.delete(eq(mockedPath)));
        }
    }

    @Test(expected = FileManager.FileException.class)
    public void testDeleteIOException() throws Exception {
        String deleteFile = "file/to/delete.jar";

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                     (mock, mctx) -> when(mock.toPath()).thenReturn(mockedPath));
             MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.delete(eq(mockedPath))).thenThrow(new IOException(""));

            testFileManager.delete(deleteFile);

            mockedFiles.verify(() -> Files.delete(eq(mockedPath)));
        }
    }

    @Test
    public void testFindHandleListFiles() throws Exception {
        File mockedFindFile1 = mock(File.class);
        File mockedFindFile2 = mock(File.class);
        String expectedResult = "directory/to/search/test-1.0.0.jar";

        when(mockedFindFile1.getPath()).thenReturn(expectedResult);

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                (mock, mctx) -> when(mock.listFiles(any(FilenameFilter.class)))
                        .thenReturn(new File[] {mockedFindFile1, mockedFindFile2}))) {

            String result = testFileManager.find(DIRECTORY_TO_SEARCH, SEARCHED_NAME);

            assertEquals(expectedResult, result);
        }
    }

    @Test
    public void testFindHandleListFilesNull() throws Exception {
        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                (mock, mctx) -> when(mock.listFiles(any(FilenameFilter.class))).thenReturn(null))) {

            String searchedFile = testFileManager.find(DIRECTORY_TO_SEARCH, SEARCHED_NAME);

            assertNull(searchedFile);
        }
    }

    @Test
    public void testFindHandleListFilesEmpty() throws Exception {
        String searchedName = "test";

        try (MockedConstruction<File> fileCons = mockConstruction(File.class,
                (mock, mctx) -> when(mock.listFiles(any(FilenameFilter.class))).thenReturn(new File[]{}))) {

            String searchedFile = testFileManager.find(DIRECTORY_TO_SEARCH, searchedName);

            assertNull(searchedFile);
        }
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
