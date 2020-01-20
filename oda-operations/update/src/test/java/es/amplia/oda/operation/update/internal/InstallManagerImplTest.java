package es.amplia.oda.operation.update.internal;

import es.amplia.oda.operation.update.DeploymentElementOperation;
import es.amplia.oda.operation.update.operations.DeploymentElementOperationFactory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;

import java.util.HashMap;
import java.util.Map;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.update.DeploymentElementOperation.DeploymentElementOperationException;
import static es.amplia.oda.operation.update.internal.InstallManagerImpl.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class InstallManagerImplTest {

    private static final String TEST_NAME_1 = "test1";
    private static final String TEST_VERSION_1 = "1.0.0";
    private static final String LOCAL_FILE = "path/to/element/to/install.jar";
    private static final String PATH_TO_BACKUP_JAR = "path/to/backup.jar";
    private static final String PATH_TO_RULES = "rules/";
    private static final String INSTALLED_DEPLOYMENT_ELEMENTS_FIELD_NAME = "installedDeploymentElements";
    private static final String INSTALL_EXCEPTION_MESSAGE = "Install exception must be thrown";

    @Mock
    private DeploymentElementOperationFactory mockedFactory;
    @InjectMocks
    private InstallManagerImpl testInstallManager;

    private final DeploymentElement installSoftwareElement =
            new DeploymentElement(TEST_NAME_1, TEST_VERSION_1, DeploymentElementType.SOFTWARE, "", "deploy/",
                    DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 0L);
    @Mock
    private DeploymentElementOperation installSoftwareOperation;
    private final DeploymentElement upgradeConfigurationElement =
            new DeploymentElement(TEST_NAME_1, TEST_VERSION_1, DeploymentElementType.CONFIGURATION, "", "configuration/",
                    DeploymentElementOperationType.UPGRADE, DeploymentElementOption.MANDATORY, 0L);
    @Mock
    private DeploymentElementOperation upgradeConfigurationOperation;
    private final DeploymentElement uninstallSoftwareElement =
            new DeploymentElement(TEST_NAME_1, TEST_VERSION_1, DeploymentElementType.SOFTWARE, "", "deploy/",
                    DeploymentElementOperationType.UNINSTALL, DeploymentElementOption.OPTIONAL, 0L);
    private final DeploymentElement installRuleElement =
            new DeploymentElement(TEST_NAME_1, TEST_VERSION_1, DeploymentElementType.RULE, "", "rules/aRule",
                    DeploymentElementOperationType.INSTALL, DeploymentElementOption.OPTIONAL, 0L);
    private final DeploymentElement uninstallAnotherElement =
            new DeploymentElement(TEST_NAME_1, TEST_VERSION_1, DeploymentElementType.DEFAULT, "", "another/dir",
                    DeploymentElementOperationType.UNINSTALL, DeploymentElementOption.OPTIONAL, 0L);
    @Mock
    private DeploymentElementOperation uninstallSoftwareOperation;

    private Map<DeploymentElement, DeploymentElementOperation> spiedEmptyInstalledElements;
    private Map<DeploymentElement, DeploymentElementOperation> spiedInstalledElements;

    @Before
    public void setUp() {
        spiedEmptyInstalledElements = spy(new HashMap<>());

        Map<DeploymentElement, DeploymentElementOperation> installedOperations = new HashMap<>();
        installedOperations.put(installSoftwareElement, installSoftwareOperation);
        installedOperations.put(upgradeConfigurationElement, upgradeConfigurationOperation);
        installedOperations.put(uninstallSoftwareElement, uninstallSoftwareOperation);
        spiedInstalledElements = spy(installedOperations);

        testInstallManager.loadConfig(PATH_TO_RULES);
    }

    @Test
    public void testAssignDeployElementSoftware() {
        assertEquals(DeploymentElementType.SOFTWARE, testInstallManager.assignDeployElementType(this.installSoftwareElement).getType());
    }

    @Test
    public void testAssignDeployElementConfiguration() {
        assertEquals(DeploymentElementType.CONFIGURATION,
                testInstallManager.assignDeployElementType(this.upgradeConfigurationElement).getType());
    }

    @Test
    public void testAssignDeployElementRule() {
        assertEquals(DeploymentElementType.RULE,
                testInstallManager.assignDeployElementType(this.installRuleElement).getType());
    }

    @Test
    public void testAssignDeployElementDefault() {
        assertEquals(DeploymentElementType.DEFAULT,
                testInstallManager.assignDeployElementType(this.uninstallAnotherElement).getType());
    }

    @Test
    public void testInstallInstallSoftware() throws InstallException, DeploymentElementOperationException {
        Whitebox.setInternalState(testInstallManager, INSTALLED_DEPLOYMENT_ELEMENTS_FIELD_NAME, spiedEmptyInstalledElements);

        when(mockedFactory.createDeploymentElementOperation(any(DeploymentElement.class), anyString(), anyString(), anyString()))
                .thenReturn(installSoftwareOperation);

        testInstallManager.install(installSoftwareElement, LOCAL_FILE);

        verify(mockedFactory).createDeploymentElementOperation (eq(installSoftwareElement),
                                                                eq(LOCAL_FILE),
                                                                eq(SOFTWARE_INSTALL_FOLDER),
                                                                eq(PATH_TO_RULES));
        verify(installSoftwareOperation).execute();
        verify(spiedEmptyInstalledElements).put(eq(installSoftwareElement), eq(installSoftwareOperation));
    }

    @Test
    public void testInstallUpgradeConfiguration() throws InstallException, DeploymentElementOperationException {
        Whitebox.setInternalState(testInstallManager, INSTALLED_DEPLOYMENT_ELEMENTS_FIELD_NAME, spiedEmptyInstalledElements);

        when(mockedFactory.createDeploymentElementOperation(any(DeploymentElement.class), anyString(), anyString(), anyString()))
                .thenReturn(upgradeConfigurationOperation);

        testInstallManager.install(upgradeConfigurationElement, LOCAL_FILE);

        verify(mockedFactory).createDeploymentElementOperation (eq(upgradeConfigurationElement),
                                                                eq(LOCAL_FILE),
                                                                eq(CONFIGURATION_INSTALL_FOLDER),
                                                                eq(PATH_TO_RULES));
        verify(upgradeConfigurationOperation).execute();
        verify(spiedEmptyInstalledElements).put(eq(upgradeConfigurationElement), eq(upgradeConfigurationOperation));
    }

    @Test
    public void testInstallUninstallSoftware() throws InstallException, DeploymentElementOperationException {
        Whitebox.setInternalState(testInstallManager, INSTALLED_DEPLOYMENT_ELEMENTS_FIELD_NAME, spiedEmptyInstalledElements);

        when(mockedFactory.createDeploymentElementOperation(any(DeploymentElement.class), anyString(), anyString(), anyString()))
                .thenReturn(uninstallSoftwareOperation);

        testInstallManager.install(uninstallSoftwareElement, LOCAL_FILE);

        verify(mockedFactory).createDeploymentElementOperation(eq(uninstallSoftwareElement),
                eq(LOCAL_FILE),
                eq(SOFTWARE_INSTALL_FOLDER),
                eq(PATH_TO_RULES));
        verify(uninstallSoftwareOperation).execute();
        verify(spiedEmptyInstalledElements).put(eq(uninstallSoftwareElement), eq(uninstallSoftwareOperation));
    }

    @Test(expected = InstallException.class)
    public void testInstallInstallSoftwareException() throws InstallException, DeploymentElementOperationException {
        Whitebox.setInternalState(testInstallManager, INSTALLED_DEPLOYMENT_ELEMENTS_FIELD_NAME, spiedInstalledElements);

        when(mockedFactory.createDeploymentElementOperation(any(DeploymentElement.class), anyString(), anyString(), anyString()))
                .thenReturn(installSoftwareOperation);
        doThrow(new DeploymentElementOperationException("")).when(installSoftwareOperation).execute();

        try {
            testInstallManager.install(installSoftwareElement, LOCAL_FILE);
        } finally {
            verify(spiedInstalledElements).put(eq(installSoftwareElement), eq(installSoftwareOperation));
        }

        fail(INSTALL_EXCEPTION_MESSAGE);
    }

    @Test(expected = InstallException.class)
    public void testInstallNotValidLocalFileInstall() throws InstallException {
        try {
            testInstallManager.install(installSoftwareElement, null);
        } finally {
            verifyZeroInteractions(spiedInstalledElements);
        }

        fail(INSTALL_EXCEPTION_MESSAGE);
    }

    @Test(expected = InstallException.class)
    public void testInstallNotValidLocalFileUpgrade() throws InstallException {
        try {
            testInstallManager.install(upgradeConfigurationElement, null);
        } finally {
            verifyZeroInteractions(spiedInstalledElements);
        }

        fail(INSTALL_EXCEPTION_MESSAGE);
    }

    @Test
    public void testRollback() throws DeploymentElementOperationException {Whitebox.setInternalState(testInstallManager, INSTALLED_DEPLOYMENT_ELEMENTS_FIELD_NAME, spiedInstalledElements);

        testInstallManager.rollback(installSoftwareElement, PATH_TO_BACKUP_JAR);

        verify(installSoftwareOperation).rollback(eq(PATH_TO_BACKUP_JAR));
    }

    @Test
    public void testRollbackNotInstalledNoException() {
        DeploymentElement notInstalledDeploymentElement =
                new DeploymentElement("", "", DeploymentElementType.SOFTWARE, "", "",
                        DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 1L);

        Whitebox.setInternalState(testInstallManager, INSTALLED_DEPLOYMENT_ELEMENTS_FIELD_NAME, spiedInstalledElements);

        testInstallManager.rollback(notInstalledDeploymentElement, PATH_TO_BACKUP_JAR);

        assertTrue("No exception is thrown", true);
    }

    @Test
    public void testRollbackDeploymentElementOperationExceptionCatch() throws DeploymentElementOperationException {
        Whitebox.setInternalState(testInstallManager, INSTALLED_DEPLOYMENT_ELEMENTS_FIELD_NAME, spiedInstalledElements);

        doThrow(new DeploymentElementOperationException("")).when(upgradeConfigurationOperation)
                .rollback(eq(PATH_TO_BACKUP_JAR));

        testInstallManager.rollback(upgradeConfigurationElement, PATH_TO_BACKUP_JAR);

        verify(upgradeConfigurationOperation).rollback(eq(PATH_TO_BACKUP_JAR));
    }

    @Test
    public void testClearInstalledDeploymentElements() {
        Whitebox.setInternalState(testInstallManager, INSTALLED_DEPLOYMENT_ELEMENTS_FIELD_NAME, spiedInstalledElements);

        testInstallManager.clearInstalledDeploymentElements();

        verify(spiedInstalledElements).clear();
    }
}