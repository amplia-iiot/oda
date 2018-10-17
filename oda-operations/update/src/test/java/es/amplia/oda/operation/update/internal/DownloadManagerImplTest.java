package es.amplia.oda.operation.update.internal;

import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.operation.update.FileManager;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.DownloadManager.DownloadException;
import static es.amplia.oda.operation.update.FileManager.FileException;
import static es.amplia.oda.operation.update.internal.DownloadManagerImpl.API_KEY_HEADER;
import static es.amplia.oda.operation.update.internal.DownloadManagerImpl.DOWNLOAD_FOLDER;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ DownloadManagerImpl.class, HttpClientBuilder.class, DataInputStream.class })
public class DownloadManagerImplTest {

    private static final String DOWNLOADED_FILE_1 = "/path/to/downloaded/file1.jar";
    private static final String NAME_1 = "testBundle1";
    private static final String VERSION_1 = "1.0.0";
    private static final String URL_1 = "http://www.platform.com/url/to/deploymentelement.jar";
    private static final DeploymentElement deploymentElement1 =
            new DeploymentElement(NAME_1, VERSION_1, DeploymentElementType.SOFTWARE, URL_1, "",
                    DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 0L);
    private static final String DOWNLOADED_FILE_2 = "/path/to/downloaded/file2.jar";
    private static final DeploymentElement deploymentElement2 =
            new DeploymentElement("test2", "2.2.2", DeploymentElementType.CONFIGURATION, "", "",
                    DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 0L);
    private static final String DOWNLOADED_FILE_3 = "/path/to/downloaded/file3.jar";
    private static final DeploymentElement deploymentElement3 =
            new DeploymentElement("test3", "3.0.0", DeploymentElementType.SOFTWARE, "", "",
                    DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 0L);
    private static final String TEST_API_KEY = "aaaaaa-111111-bbbbbbb-2222-ccdd-12345677890";
    private static final String DOWNLOADED_FILES_FIELD_NAME = "downloadedFiles";
    private static final String TEST_VERSION = "1.0.0";

    @Mock
    private DeviceInfoProvider mockedDeviceInfoProvider;
    @Mock
    private FileManager mockedFileManager;
    @InjectMocks
    private DownloadManagerImpl testDownloadManager;

    private Map<DeploymentElement, String> spiedDownloadedFiles;

    @Before
    public void setUp() {
        Map<DeploymentElement, String> downloadedFiles = new HashMap<>();
        downloadedFiles.put(deploymentElement1, DOWNLOADED_FILE_1);
        downloadedFiles.put(deploymentElement2, DOWNLOADED_FILE_2);
        downloadedFiles.put(deploymentElement3, DOWNLOADED_FILE_3);
        spiedDownloadedFiles = spy(downloadedFiles);
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

    @Test(expected = DownloadException.class)
    public void testCreateDownloadDirectoryFileException() throws FileException, DownloadException {
        when(mockedFileManager.exist(eq(DOWNLOAD_FOLDER))).thenReturn(false);
        doThrow(new FileException("")).when(mockedFileManager).createDirectory(eq(DOWNLOAD_FOLDER));

        testDownloadManager.createDownloadDirectory();

        fail("File exception must be thrown");
    }

    @Test
    public void testDownloadSoftware() throws Exception {
        HttpGet mockedHttpGet = mock(HttpGet.class);
        HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockedEntity = mock(HttpEntity.class);
        InputStream mockedContent = mock(InputStream.class);
        DataInputStream mockedReader = PowerMockito.mock(DataInputStream.class);
        File mockedFile = mock(File.class);
        FileOutputStream mockedFileOutputStream = mock(FileOutputStream.class);
        DataOutputStream mockedWriter = mock(DataOutputStream.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);
        String downloadedFile = DOWNLOAD_FOLDER + NAME_1 + "-" + VERSION_1 + ".jar";

        PowerMockito.whenNew(HttpGet.class).withAnyArguments().thenReturn(mockedHttpGet);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpGet.class))).thenReturn(mockedResponse);
        when(mockedResponse.getEntity()).thenReturn(mockedEntity);
        when(mockedEntity.getContent()).thenReturn(mockedContent);
        PowerMockito.whenNew(DataInputStream.class).withAnyArguments().thenReturn(mockedReader);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(mockedFileOutputStream);
        PowerMockito.whenNew(DataOutputStream.class).withAnyArguments().thenReturn(mockedWriter);
        when(mockedResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(200);
        PowerMockito.when(mockedReader.read(any())).thenReturn(4096).thenReturn(2048).thenReturn(-1);

        testDownloadManager.download(deploymentElement1);

        PowerMockito.verifyNew(HttpGet.class).withArguments(eq(URL_1));
        verify(mockedHttpGet).addHeader(eq(API_KEY_HEADER), eq(TEST_API_KEY));
        verify(mockedClient).execute(eq(mockedHttpGet));
        PowerMockito.verifyNew(DataInputStream.class).withArguments(mockedContent);
        PowerMockito.verifyNew(File.class).withArguments(downloadedFile);

        //noinspection ResultOfMethodCallIgnored
        verify(mockedReader, times(3)).read(any());
        verify(mockedWriter, times(2)).write(any(), anyInt(), anyInt());
        spiedDownloadedFiles.put(eq(deploymentElement1), eq(downloadedFile));

        verify(mockedClient).close();
        mockedResponse.close();
        mockedReader.close();
        mockedWriter.close();
    }

    @Test
    public void testDownloadConfiguration() throws Exception {
        String configurationDeploymentElementName = "configurationBundle";
        DeploymentElement configurationDeploymentElement =
                new DeploymentElement(configurationDeploymentElementName, TEST_VERSION, DeploymentElementType.CONFIGURATION,
                        URL_1, "", DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 0L);
        HttpGet mockedHttpGet = mock(HttpGet.class);
        HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockedEntity = mock(HttpEntity.class);
        InputStream mockedContent = mock(InputStream.class);
        DataInputStream mockedReader = PowerMockito.mock(DataInputStream.class);
        File mockedFile = mock(File.class);
        FileOutputStream mockedFileOutputStream = mock(FileOutputStream.class);
        DataOutputStream mockedWriter = mock(DataOutputStream.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);
        String downloadedFile = DOWNLOAD_FOLDER + configurationDeploymentElementName + ".cfg";

        PowerMockito.whenNew(HttpGet.class).withAnyArguments().thenReturn(mockedHttpGet);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpGet.class))).thenReturn(mockedResponse);
        when(mockedResponse.getEntity()).thenReturn(mockedEntity);
        when(mockedEntity.getContent()).thenReturn(mockedContent);
        PowerMockito.whenNew(DataInputStream.class).withAnyArguments().thenReturn(mockedReader);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(mockedFileOutputStream);
        PowerMockito.whenNew(DataOutputStream.class).withAnyArguments().thenReturn(mockedWriter);
        when(mockedResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(200);
        PowerMockito.when(mockedReader.read(any())).thenReturn(4096).thenReturn(2048).thenReturn(-1);

        testDownloadManager.download(configurationDeploymentElement);

        PowerMockito.verifyNew(HttpGet.class).withArguments(eq(URL_1));
        verify(mockedHttpGet).addHeader(eq(API_KEY_HEADER), eq(TEST_API_KEY));
        verify(mockedClient).execute(eq(mockedHttpGet));
        PowerMockito.verifyNew(DataInputStream.class).withArguments(mockedContent);
        PowerMockito.verifyNew(File.class).withArguments(downloadedFile);

        //noinspection ResultOfMethodCallIgnored
        verify(mockedReader, times(3)).read(any());
        verify(mockedWriter, times(2)).write(any(), anyInt(), anyInt());
        spiedDownloadedFiles.put(eq(configurationDeploymentElement), eq(downloadedFile));

        verify(mockedClient).close();
        mockedResponse.close();
        mockedReader.close();
        mockedWriter.close();
    }

    @Test
    public void testDownloadOtherType() throws Exception {
        String otherTypeDeploymentElementName = "otherTypeBundle";
        DeploymentElement otherDeploymentElement =
                new DeploymentElement(otherTypeDeploymentElementName, TEST_VERSION, DeploymentElementType.FIRMWARE,
                        URL_1, "", DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 0L);
        HttpGet mockedHttpGet = mock(HttpGet.class);
        HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockedEntity = mock(HttpEntity.class);
        InputStream mockedContent = mock(InputStream.class);
        DataInputStream mockedReader = PowerMockito.mock(DataInputStream.class);
        File mockedFile = mock(File.class);
        FileOutputStream mockedFileOutputStream = mock(FileOutputStream.class);
        DataOutputStream mockedWriter = mock(DataOutputStream.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);
        String downloadedFile = DOWNLOAD_FOLDER + otherTypeDeploymentElementName;

        PowerMockito.whenNew(HttpGet.class).withAnyArguments().thenReturn(mockedHttpGet);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpGet.class))).thenReturn(mockedResponse);
        when(mockedResponse.getEntity()).thenReturn(mockedEntity);
        when(mockedEntity.getContent()).thenReturn(mockedContent);
        PowerMockito.whenNew(DataInputStream.class).withAnyArguments().thenReturn(mockedReader);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(mockedFileOutputStream);
        PowerMockito.whenNew(DataOutputStream.class).withAnyArguments().thenReturn(mockedWriter);
        when(mockedResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(200);
        PowerMockito.when(mockedReader.read(any())).thenReturn(4096).thenReturn(2048).thenReturn(-1);

        testDownloadManager.download(otherDeploymentElement);

        PowerMockito.verifyNew(HttpGet.class).withArguments(eq(URL_1));
        verify(mockedHttpGet).addHeader(eq(API_KEY_HEADER), eq(TEST_API_KEY));
        verify(mockedClient).execute(eq(mockedHttpGet));
        PowerMockito.verifyNew(DataInputStream.class).withArguments(mockedContent);
        PowerMockito.verifyNew(File.class).withArguments(downloadedFile);

        //noinspection ResultOfMethodCallIgnored
        verify(mockedReader, times(3)).read(any());
        verify(mockedWriter, times(2)).write(any(), anyInt(), anyInt());
        spiedDownloadedFiles.put(eq(otherDeploymentElement), eq(downloadedFile));

        verify(mockedClient).close();
        mockedResponse.close();
        mockedReader.close();
        mockedWriter.close();
    }

    @Test(expected = DownloadException.class)
    public void testDownloadHttpGetExecuteError() throws Exception {
        HttpGet mockedHttpGet = mock(HttpGet.class);
        HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);

        PowerMockito.whenNew(HttpGet.class).withAnyArguments().thenReturn(mockedHttpGet);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpGet.class))).thenThrow(new IOException(""));

        testDownloadManager.download(deploymentElement1);

        fail("Download exception must be thrown");
    }

    @Test(expected = DownloadException.class)
    public void testDownloadHttpResponseError() throws Exception {
        HttpGet mockedHttpGet = mock(HttpGet.class);
        HttpClientBuilder mockedBuilder = mock(HttpClientBuilder.class);
        CloseableHttpClient mockedClient = mock(CloseableHttpClient.class);
        CloseableHttpResponse mockedResponse = mock(CloseableHttpResponse.class);
        HttpEntity mockedEntity = mock(HttpEntity.class);
        InputStream mockedContent = mock(InputStream.class);
        DataInputStream mockedReader = PowerMockito.mock(DataInputStream.class);
        File mockedFile = mock(File.class);
        FileOutputStream mockedFileOutputStream = mock(FileOutputStream.class);
        DataOutputStream mockedWriter = mock(DataOutputStream.class);
        StatusLine mockedStatusLine = mock(StatusLine.class);

        PowerMockito.whenNew(HttpGet.class).withAnyArguments().thenReturn(mockedHttpGet);
        when(mockedDeviceInfoProvider.getApiKey()).thenReturn(TEST_API_KEY);
        PowerMockito.mockStatic(HttpClientBuilder.class);
        PowerMockito.when(HttpClientBuilder.create()).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedClient);
        when(mockedClient.execute(any(HttpGet.class))).thenReturn(mockedResponse);
        when(mockedResponse.getEntity()).thenReturn(mockedEntity);
        when(mockedEntity.getContent()).thenReturn(mockedContent);
        PowerMockito.whenNew(DataInputStream.class).withAnyArguments().thenReturn(mockedReader);
        PowerMockito.whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
        PowerMockito.whenNew(FileOutputStream.class).withAnyArguments().thenReturn(mockedFileOutputStream);
        PowerMockito.whenNew(DataOutputStream.class).withAnyArguments().thenReturn(mockedWriter);
        when(mockedResponse.getStatusLine()).thenReturn(mockedStatusLine);
        when(mockedStatusLine.getStatusCode()).thenReturn(404);

        testDownloadManager.download(deploymentElement1);

        fail("Download exception must be thrown");
    }

    @Test
    public void testGetDownloadedFile() {
        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, spiedDownloadedFiles);

        assertEquals(DOWNLOADED_FILE_3, testDownloadManager.getDownloadedFile(deploymentElement3));
        assertEquals(DOWNLOADED_FILE_1, testDownloadManager.getDownloadedFile(deploymentElement1));
        assertEquals(DOWNLOADED_FILE_2, testDownloadManager.getDownloadedFile(deploymentElement2));
    }

    @Test
    public void testGetDownloadedFileNotFound() {
        DeploymentElement nonExistentDeploymentElement =
                new DeploymentElement("nonexistent", "0.0.0", DeploymentElementType.CONFIGURATION, "", "",
                        DeploymentElementOperationType.UPGRADE, DeploymentElementOption.OPTIONAL, 2L);

        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, spiedDownloadedFiles);

        assertNull(testDownloadManager.getDownloadedFile(nonExistentDeploymentElement));
    }

    @Test
    public void testDeleteDownloadedFiles() throws FileException {
        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, spiedDownloadedFiles);

        testDownloadManager.deleteDownloadedFiles();

        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_1));
        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_2));
        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_3));
        verify(spiedDownloadedFiles).clear();
    }

    @Test
    public void testDeleteDownloadedFilesCatchException() throws FileException {
        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, spiedDownloadedFiles);

        doThrow(new FileManager.FileException("")).when(mockedFileManager).delete(eq(DOWNLOADED_FILE_2));

        testDownloadManager.deleteDownloadedFiles();

        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_1));
        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_2));
        verify(mockedFileManager).delete(eq(DOWNLOADED_FILE_3));
        verify(spiedDownloadedFiles).clear();
    }

    @Test
    public void testDeleteDownloadedFilesEmptyBackupFiles() {
        Whitebox.setInternalState(testDownloadManager, DOWNLOADED_FILES_FIELD_NAME, Collections.emptyMap());

        testDownloadManager.deleteDownloadedFiles();

        verifyZeroInteractions(mockedFileManager);
    }
}