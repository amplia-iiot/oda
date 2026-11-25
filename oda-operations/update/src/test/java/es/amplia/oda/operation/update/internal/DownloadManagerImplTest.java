package es.amplia.oda.operation.update.internal;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElement;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElementOperationType;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElementOption;
import es.amplia.oda.operation.api.OperationUpdate.DeploymentElementType;
import es.amplia.oda.operation.update.FileManager;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import javax.net.ssl.SSLContext;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static es.amplia.oda.operation.update.DownloadManager.DownloadException;
import static es.amplia.oda.operation.update.FileManager.FileException;
import static es.amplia.oda.operation.update.internal.DownloadManagerImpl.API_KEY_HEADER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DownloadManagerImplTest {

    private static final String DOWNLOAD_FOLDER = "downloads/";
    private static final String DOWNLOADED_FILE_1 = "/path/to/downloaded/file1.jar";
    private static final String NAME_1 = "testBundle1";
    private static final String VERSION_1 = "1.0.0";
    private static final String URL_1 = "https://www.platform.com/url/to/deploymentelement.jar";
    private static final DeploymentElement deploymentElement1 =
            new DeploymentElement(NAME_1, VERSION_1, DeploymentElementType.SOFTWARE, URL_1, "deploy/", 1L,
                    DeploymentElementOperationType.INSTALL,
                    Collections.EMPTY_LIST, 0L, "0.0.9",
                    DeploymentElementOption.MANDATORY);
    private static final String DOWNLOADED_FILE_2 = "/path/to/downloaded/file2.jar";
    private static final DeploymentElement deploymentElement2 =
            new DeploymentElement("test2", "2.2.2", DeploymentElementType.CONFIGURATION, "", "configuration/", 1L,
                    DeploymentElementOperationType.INSTALL,
                    Collections.EMPTY_LIST, 0L, "0.0.9",
                    DeploymentElementOption.MANDATORY);
    private static final String DOWNLOADED_FILE_3 = "/path/to/downloaded/file3.jar";
    private static final DeploymentElement deploymentElement3 =
            new DeploymentElement("test3", "3.0.0", DeploymentElementType.SOFTWARE, "", "deploy/", 1L,
                    DeploymentElementOperationType.INSTALL,
                    Collections.EMPTY_LIST, 0L, "0.0.9",
                    DeploymentElementOption.MANDATORY);
    private static final String TEST_API_KEY = "testApiKey";
    private static final String DOWNLOADED_FILES_FIELD_NAME = "downloadedFiles";
    private static final String TEST_VERSION = "1.0.0";

    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private FileManager mockedFileManager;
    @InjectMocks
    private DownloadManagerImpl testDownloadManager;

    private Map<DeploymentElement, String> testDownloadedFiles;

    @TempDir
    Path tempDir;

    @BeforeAll
    public static void loadClassesUsedInsideMockedScopes() throws Exception {
        Class.forName(DownloadManagerImpl.class.getName() + "$1");
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
    }

    @BeforeEach
    public void setUp() {
        testDownloadedFiles = new HashMap<>();
        testDownloadedFiles.put(deploymentElement1, DOWNLOADED_FILE_1);
        testDownloadedFiles.put(deploymentElement2, DOWNLOADED_FILE_2);
        testDownloadedFiles.put(deploymentElement3, DOWNLOADED_FILE_3);

        testDownloadManager.loadConfig("rules/", "jslib/", "deploy/",
                "configuration/", "downloads/");
    }

    @Test
    public void testCreateDownloadDirectory() throws DownloadException, FileException {
        when(mockedFileManager.exist(eq(DOWNLOAD_FOLDER))).thenReturn(false);

        testDownloadManager.createDownloadDirectory();

        verify(mockedFileManager).createDirectory(eq(DOWNLOAD_FOLDER));
    }

    @Test
    public void testCreateDownloadDirectoryAlreadyExists() throws DownloadException, FileException {
        when(mockedFileManager.exist(eq(DOWNLOAD_FOLDER))).thenReturn(true);

        testDownloadManager.createDownloadDirectory();

        verify(mockedFileManager, never()).createDirectory(eq(DOWNLOAD_FOLDER));
    }

    @Test
    public void testCreateDownloadDirectoryFileException() throws FileException, DownloadException {
        when(mockedFileManager.exist(eq(DOWNLOAD_FOLDER))).thenReturn(false);
        doThrow(new FileException("")).when(mockedFileManager).createDirectory(eq(DOWNLOAD_FOLDER));

        assertThrows(DownloadException.class, () -> testDownloadManager.createDownloadDirectory());
    }

    @Test
    public void testDownloadSoftware() throws Exception {
        assertDownloads(deploymentElement1, NAME_1 + "-" + VERSION_1 + ".jar");
    }

    @Test
    public void testDownloadConfiguration() throws Exception {
        String configurationDeploymentElementName = "configurationBundle";
        DeploymentElement configurationDeploymentElement =
                new DeploymentElement(configurationDeploymentElementName, TEST_VERSION, DeploymentElementType.CONFIGURATION,
                        URL_1, "configuration/", 1L, DeploymentElementOperationType.INSTALL,
                        Collections.EMPTY_LIST, 0L, "0.0.9", DeploymentElementOption.MANDATORY);

        assertDownloads(configurationDeploymentElement, configurationDeploymentElementName + ".cfg");
    }

    @Test
    public void testDownloadOtherType() throws Exception {
        String otherTypeDeploymentElementName = "otherTypeBundle";
        DeploymentElement otherDeploymentElement =
                new DeploymentElement(otherTypeDeploymentElementName, TEST_VERSION, DeploymentElementType.FIRMWARE,
                        URL_1, "", 1L, DeploymentElementOperationType.INSTALL,
                        Collections.EMPTY_LIST, 0L, "0.0.9",
                        DeploymentElementOption.MANDATORY);

        assertDownloads(otherDeploymentElement, otherTypeDeploymentElementName);
    }

    // Writes to a real temporary folder: mocking File or the JDK streams also intercepts the
    // ones the JVM itself creates (class loading), which breaks other tests depending on order
    private void assertDownloads(DeploymentElement element, String expectedFileName) throws Exception {
        String downloadsPath = tempDir.toString() + File.separator;
        testDownloadManager.loadConfig("rules/", "jslib/", "deploy/", "configuration/", downloadsPath);
        byte[] content = new byte[6144];
        new Random(42).nextBytes(content);

        HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockedEntity = mock(HttpEntity.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);

        List<List<?>> httpGetArgs = new ArrayList<>();
        try (MockedConstruction<HttpGet> httpGetCons = mockConstruction(HttpGet.class,
                     (mock, mctx) -> httpGetArgs.add(new ArrayList<>(mctx.arguments())));
             MockedStatic<HttpClientBuilder> mockedHttpClientBuilder = mockStatic(HttpClientBuilder.class)) {

            when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
            mockedHttpClientBuilder.when(HttpClientBuilder::create).thenReturn(mockedBuilder);
            when(mockedBuilder.setSSLContext(any())).thenReturn(mockedBuilder);
            when(mockedBuilder.build()).thenReturn(mockedClient);
            when(mockedClient.execute(any(HttpGet.class))).thenReturn(mockedResponse);
            when(mockedResponse.getEntity()).thenReturn(mockedEntity);
            when(mockedEntity.getContent()).thenReturn(new ByteArrayInputStream(content));
            when(mockedResponse.getStatusLine()).thenReturn(mockedStatusLine);
            when(mockedStatusLine.getStatusCode()).thenReturn(200);

            testDownloadManager.download(element);

            HttpGet mockedHttpGet = httpGetCons.constructed().get(0);
            assertEquals(URL_1, httpGetArgs.get(0).get(0));
            verify(mockedHttpGet).addHeader(eq(API_KEY_HEADER), eq(TEST_API_KEY));
            verify(mockedClient).execute(eq(mockedHttpGet));
            verify(mockedClient).close();

            String downloadedFile = downloadsPath + expectedFileName;
            assertEquals(downloadedFile, testDownloadManager.getDownloadedFile(element));
            assertArrayEquals(content, Files.readAllBytes(Paths.get(downloadedFile)));
        }
    }

    @Test
    public void testDownloadHttpGetExecuteError() throws Exception {
        HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);

        try (MockedConstruction<HttpGet> httpGetCons = mockConstruction(HttpGet.class);
             MockedStatic<HttpClientBuilder> mockedHttpClientBuilder = mockStatic(HttpClientBuilder.class)) {

            when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
            mockedHttpClientBuilder.when(HttpClientBuilder::create).thenReturn(mockedBuilder);
            when(mockedBuilder.setSSLContext(any())).thenReturn(mockedBuilder);
            when(mockedBuilder.build()).thenReturn(mockedClient);
            when(mockedClient.execute(any(HttpGet.class))).thenThrow(new IOException(""));

            assertThrows(DownloadException.class, () -> testDownloadManager.download(deploymentElement1));
        }
    }

    @Test
    public void testDownloadHttpResponseError() throws Exception {
        testDownloadManager.loadConfig("rules/", "jslib/", "deploy/", "configuration/",
                tempDir.toString() + File.separator);
        HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockedEntity = mock(HttpEntity.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);

        try (MockedConstruction<HttpGet> httpGetCons = mockConstruction(HttpGet.class);
             MockedStatic<HttpClientBuilder> mockedHttpClientBuilder = mockStatic(HttpClientBuilder.class)) {

            when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
            mockedHttpClientBuilder.when(HttpClientBuilder::create).thenReturn(mockedBuilder);
            when(mockedBuilder.setSSLContext(any())).thenReturn(mockedBuilder);
            when(mockedBuilder.build()).thenReturn(mockedClient);
            when(mockedClient.execute(any(HttpGet.class))).thenReturn(mockedResponse);
            when(mockedResponse.getEntity()).thenReturn(mockedEntity);
            when(mockedEntity.getContent()).thenReturn(new ByteArrayInputStream(new byte[0]));
            when(mockedResponse.getStatusLine()).thenReturn(mockedStatusLine);
            when(mockedStatusLine.getStatusCode()).thenReturn(404);

            assertThrows(DownloadException.class, () -> testDownloadManager.download(deploymentElement1));
        }
    }

    @Test
    public void testGetDownloadedFile() {
        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, testDownloadedFiles);

        assertEquals(DOWNLOADED_FILE_3, testDownloadManager.getDownloadedFile(deploymentElement3));
        assertEquals(DOWNLOADED_FILE_1, testDownloadManager.getDownloadedFile(deploymentElement1));
        assertEquals(DOWNLOADED_FILE_2, testDownloadManager.getDownloadedFile(deploymentElement2));
    }

    @Test
    public void testGetDownloadedFileNotFound() {
        DeploymentElement nonExistentDeploymentElement =
                new DeploymentElement("nonexistent", "0.0.0", DeploymentElementType.CONFIGURATION, "", "", 1L,
                        DeploymentElementOperationType.UPGRADE,
                        Collections.EMPTY_LIST, 0L, "0.0.9",
                        DeploymentElementOption.OPTIONAL);

        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, testDownloadedFiles);

        assertNull(testDownloadManager.getDownloadedFile(nonExistentDeploymentElement));
    }

    @Test
    public void testDeleteDownloadedFiles() throws FileException {
        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, testDownloadedFiles);

        testDownloadManager.deleteDownloadedFiles();

        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_1));
        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_2));
        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_3));
        assertTrue(testDownloadedFiles.isEmpty());
    }

    @Test
    public void testDeleteDownloadedFilesCatchException() throws FileException {
        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, testDownloadedFiles);

        doThrow(new FileManager.FileException("")).when(mockedFileManager).delete(eq(DOWNLOADED_FILE_2));

        testDownloadManager.deleteDownloadedFiles();

        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_1));
        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_2));
        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_3));
        assertTrue(testDownloadedFiles.isEmpty());
    }

    @Test
    public void testDeleteDownloadedFilesEmptyBackupFiles() {
        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, Collections.emptyMap());

        testDownloadManager.deleteDownloadedFiles();

        verifyNoInteractions(mockedFileManager);
    }

    @Test
    public void testDeleteDownloadedFilesWithNullValues() throws FileException {
        Map<DeploymentElement, String> downloadedFiles = new HashMap<>();
        downloadedFiles.put(deploymentElement1, DOWNLOADED_FILE_1);
        downloadedFiles.put(deploymentElement2, null);
        downloadedFiles.put(deploymentElement3, DOWNLOADED_FILE_3);

        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, downloadedFiles);

        testDownloadManager.deleteDownloadedFiles();

        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_1));
        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_3));
        assertTrue(downloadedFiles.isEmpty());
    }
}
