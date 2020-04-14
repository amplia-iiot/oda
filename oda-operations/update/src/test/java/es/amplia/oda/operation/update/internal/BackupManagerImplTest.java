package es.amplia.oda.operation.update.internal;

import es.amplia.oda.operation.api.OperationUpdate;
import es.amplia.oda.operation.update.BackupManager;
import es.amplia.oda.operation.update.FileManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.internal.BackupManagerImpl.BACKUP_FOLDER;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BackupManagerImplTest {

    private static final String DEPLOYMENT_ELEMENT_NAME = "testBundle";
    private static final DeploymentElement DEPLOYMENT_ELEMENT =
            new DeploymentElement(DEPLOYMENT_ELEMENT_NAME, "",
                    OperationUpdate.DeploymentElementType.SOFTWARE, "", "", 1L,
                    OperationUpdate.DeploymentElementOperationType.INSTALL,
                    Collections.EMPTY_LIST, 0L, "0.0.9",
                    OperationUpdate.DeploymentElementOption.MANDATORY);
    private static final String BASE_PATH = "path/to/bundles/";
    private static final String FILE_TO_BACKUP = "path/to/bundles/test-1.1.1.jar";

    private static final String DEPLOYMENT_ELEMENT_NAME_1 = "testBundle1";
    private static final DeploymentElement DEPLOYMENT_ELEMENT_1 =
            new DeploymentElement(DEPLOYMENT_ELEMENT_NAME_1, "",
                    OperationUpdate.DeploymentElementType.SOFTWARE, "", "", 1L,
                    OperationUpdate.DeploymentElementOperationType.INSTALL,
                    Collections.EMPTY_LIST, 0L, "0.0.9",
                    OperationUpdate.DeploymentElementOption.MANDATORY);
    private static final String BACKUP_FILE_1 = BACKUP_FOLDER + DEPLOYMENT_ELEMENT_NAME_1;
    private static final String DEPLOYMENT_ELEMENT_NAME_2 = "testBundle2";
    private static final DeploymentElement DEPLOYMENT_ELEMENT_2 =
            new DeploymentElement(DEPLOYMENT_ELEMENT_NAME_2, "",
                    OperationUpdate.DeploymentElementType.CONFIGURATION, "", "", 1L,
                    OperationUpdate.DeploymentElementOperationType.UPGRADE,
                    Collections.EMPTY_LIST, 0L, "0.0.9",
                    OperationUpdate.DeploymentElementOption.OPTIONAL);
    private static final String BACKUP_FILE_2 = BACKUP_FOLDER + DEPLOYMENT_ELEMENT_NAME_2;
    private static final String DEPLOYMENT_ELEMENT_NAME_3 = "testBundle3";
    private static final DeploymentElement DEPLOYMENT_ELEMENT_3 =
            new DeploymentElement(DEPLOYMENT_ELEMENT_NAME_3, "",
                    OperationUpdate.DeploymentElementType.SOFTWARE, "", "", 1L,
                    OperationUpdate.DeploymentElementOperationType.UNINSTALL,
                    Collections.EMPTY_LIST, 0L, "0.0.9",
                    OperationUpdate.DeploymentElementOption.OPTIONAL);
    private static final String BACKUP_FILE_3 = BACKUP_FOLDER + DEPLOYMENT_ELEMENT_NAME_3;
    private static final String BACKUP_FILES_FIELD_NAME = "backupFiles";

    @Mock
    private FileManager mockedFileManager;
    @InjectMocks
    private BackupManagerImpl testBackupManager;

    private Map<DeploymentElement, String> spiedBackups;

    @Before
    public void setUp() {
        Map<DeploymentElement, String> backups = new HashMap<>();
        backups.put(DEPLOYMENT_ELEMENT_1, BACKUP_FILE_1);
        backups.put(DEPLOYMENT_ELEMENT_2, BACKUP_FILE_2);
        backups.put(DEPLOYMENT_ELEMENT_3, BACKUP_FILE_3);
        spiedBackups = spy(backups);
    }

    @Test
    public void testCreateBackupDirectory() throws BackupManager.BackupException, FileManager.FileException {
        when(mockedFileManager.exist(eq(BACKUP_FOLDER))).thenReturn(false);

        testBackupManager.createBackupDirectory();

        verify(mockedFileManager).createDirectory(eq(BACKUP_FOLDER));
    }

    @Test
    public void testCreateBackupDirectoryAlreadyExists() throws BackupManager.BackupException,
            FileManager.FileException {
        when(mockedFileManager.exist(eq(BACKUP_FOLDER))).thenReturn(true);

        testBackupManager.createBackupDirectory();

        verify(mockedFileManager, never()).createDirectory(eq(BACKUP_FOLDER));
    }

    @Test(expected = BackupManager.BackupException.class)
    public void testCreateBackupDirectoryFileException() throws FileManager.FileException,
            BackupManager.BackupException {
        when(mockedFileManager.exist(eq(BACKUP_FOLDER))).thenReturn(false);
        doThrow(new FileManager.FileException("")).when(mockedFileManager).createDirectory(eq(BACKUP_FOLDER));

        testBackupManager.createBackupDirectory();

        fail("File exception must be thrown");
    }

    @Test
    public void testBackup() throws FileManager.FileException, BackupManager.BackupException {
        String backupFile = BASE_PATH + DEPLOYMENT_ELEMENT_NAME;

        Whitebox.setInternalState(testBackupManager, BACKUP_FILES_FIELD_NAME, spiedBackups);
        when(mockedFileManager.find(eq(BASE_PATH), eq(DEPLOYMENT_ELEMENT_NAME))).thenReturn(FILE_TO_BACKUP);
        when(mockedFileManager.copy(anyString(), anyString())).thenReturn(backupFile);


        testBackupManager.backup(DEPLOYMENT_ELEMENT, BASE_PATH);

        verify(mockedFileManager).copy(eq(FILE_TO_BACKUP), eq(BACKUP_FOLDER));
        verify(spiedBackups).put(eq(DEPLOYMENT_ELEMENT), eq(backupFile));
    }

    @Test(expected = BackupManager.BackupException.class)
    public void testBackupNoFileFound() throws BackupManager.BackupException {
        when(mockedFileManager.find(eq(BASE_PATH), eq(DEPLOYMENT_ELEMENT_NAME))).thenReturn(null);

        testBackupManager.backup(DEPLOYMENT_ELEMENT, BASE_PATH);

        fail("Backup Exception must not be thrown");
    }

    @Test(expected = BackupManager.BackupException.class)
    public void testBackupNoFileManagerException() throws FileManager.FileException, BackupManager.BackupException {
        when(mockedFileManager.find(eq(BASE_PATH), eq(DEPLOYMENT_ELEMENT_NAME))).thenReturn(FILE_TO_BACKUP);
        doThrow(new FileManager.FileException("")).when(mockedFileManager).copy(eq(FILE_TO_BACKUP), eq(BACKUP_FOLDER));

        testBackupManager.backup(DEPLOYMENT_ELEMENT, BASE_PATH);

        fail("Backup Exception must not be thrown");
    }

    @Test
    public void testGetBackupFile() {
        Whitebox.setInternalState(testBackupManager, BACKUP_FILES_FIELD_NAME, spiedBackups);

        assertEquals(BACKUP_FILE_1, testBackupManager.getBackupFile(DEPLOYMENT_ELEMENT_1));
        assertEquals(BACKUP_FILE_2, testBackupManager.getBackupFile(DEPLOYMENT_ELEMENT_2));
        assertEquals(BACKUP_FILE_3, testBackupManager.getBackupFile(DEPLOYMENT_ELEMENT_3));
    }

    @Test
    public void testGetBackupFileNoDeploymentElementFound() {
        DeploymentElement nonExistentDeploymentElement =
                new DeploymentElement("","",DeploymentElementType.SOFTWARE, "", "", 1L,
                        DeploymentElementOperationType.INSTALL,
                        Collections.EMPTY_LIST, 0L, "0.0.9",
                        DeploymentElementOption.MANDATORY);

        assertNull(testBackupManager.getBackupFile(nonExistentDeploymentElement));
    }

    @Test
    public void testDeleteBackupFiles() throws FileManager.FileException {
        Whitebox.setInternalState(testBackupManager, BACKUP_FILES_FIELD_NAME, spiedBackups);

        testBackupManager.deleteBackupFiles();

        verify(mockedFileManager).delete(eq(BACKUP_FILE_1));
        verify(mockedFileManager).delete(eq(BACKUP_FILE_2));
        verify(mockedFileManager).delete(eq(BACKUP_FILE_3));
        verify(spiedBackups).clear();
    }

    @Test
    public void testDeleteBackupFilesCatchException() throws FileManager.FileException {
        Whitebox.setInternalState(testBackupManager, BACKUP_FILES_FIELD_NAME, spiedBackups);

        doThrow(new FileManager.FileException("")).when(mockedFileManager).delete(eq(BACKUP_FILE_2));

        testBackupManager.deleteBackupFiles();

        verify(mockedFileManager).delete(eq(BACKUP_FILE_1));
        verify(mockedFileManager).delete(eq(BACKUP_FILE_2));
        verify(mockedFileManager).delete(eq(BACKUP_FILE_3));
        verify(spiedBackups).clear();
    }

    @Test
    public void testDeleteBackupFilesEmptyBackupFiles() {
        Whitebox.setInternalState(testBackupManager, BACKUP_FILES_FIELD_NAME, Collections.emptyMap());

        testBackupManager.deleteBackupFiles();

        verifyZeroInteractions(mockedFileManager);
    }

    @Test
    public void testDeleteBackupFilesWithNullValues() throws FileManager.FileException {
        Map<DeploymentElement, String> backups = new HashMap<>();
        backups.put(DEPLOYMENT_ELEMENT_1, BACKUP_FILE_1);
        backups.put(DEPLOYMENT_ELEMENT_2, BACKUP_FILE_2);
        backups.put(DEPLOYMENT_ELEMENT_3, BACKUP_FILE_3);
        spiedBackups = spy(backups);

        Whitebox.setInternalState(testBackupManager, BACKUP_FILES_FIELD_NAME, spiedBackups);

        testBackupManager.deleteBackupFiles();

        verify(mockedFileManager).delete(eq(BACKUP_FILE_1));
        verify(mockedFileManager).delete(eq(BACKUP_FILE_3));
        verify(spiedBackups).clear();
    }
}
