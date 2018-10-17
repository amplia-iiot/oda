package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static es.amplia.oda.operation.api.OperationUpdate.*;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DeploymentElementOperationFactory.class)
public class DeploymentElementOperationFactoryTest {

    private static final String localFile = "path/to/local/file.jar";
    private static final String installFolder = "path/to/install/folder";

    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;
    @InjectMocks
    private DeploymentElementOperationFactory testFactory;

    @Test
    public void testCreateInstallDeploymentElementOperation() throws Exception {
        DeploymentElement installDeploymentElement =
                new DeploymentElement("","", DeploymentElementType.SOFTWARE, "", "",
                        DeploymentElementOperationType.INSTALL, DeploymentElementOption.MANDATORY, 1L);
        InstallDeploymentElementOperation mockedInstallOp = mock(InstallDeploymentElementOperation.class);

        PowerMockito.whenNew(InstallDeploymentElementOperation.class).withAnyArguments().thenReturn(mockedInstallOp);

        testFactory.createDeploymentElementOperation(installDeploymentElement, localFile, installFolder);

        PowerMockito.verifyNew(InstallDeploymentElementOperation.class)
                .withArguments(eq(installDeploymentElement), eq(localFile), eq(installFolder), eq(mockedFileManager),
                        eq(mockedOperationConfirmationProcessor));
    }

    @Test
    public void testCreateUpgradeDeploymentElementOperation() throws Exception {
        DeploymentElement upgradeDeploymentElement =
                new DeploymentElement("","", DeploymentElementType.CONFIGURATION, "", "",
                        DeploymentElementOperationType.UPGRADE, DeploymentElementOption.OPTIONAL, 1L);
        UpgradeDeploymentElementOperation mockedUpgradeOp = mock(UpgradeDeploymentElementOperation.class);

        PowerMockito.whenNew(UpgradeDeploymentElementOperation.class).withAnyArguments().thenReturn(mockedUpgradeOp);

        testFactory.createDeploymentElementOperation(upgradeDeploymentElement, localFile, installFolder);

        PowerMockito.verifyNew(UpgradeDeploymentElementOperation.class)
                .withArguments(eq(upgradeDeploymentElement), eq(localFile), eq(installFolder), eq(mockedFileManager),
                        eq(mockedOperationConfirmationProcessor));
    }

    @Test
    public void testCreateDeleteDeploymentElementOperation() throws Exception {
        DeploymentElement uninstallDeploymentElement =
                new DeploymentElement("","", DeploymentElementType.SOFTWARE, "", "",
                        DeploymentElementOperationType.UNINSTALL, DeploymentElementOption.OPTIONAL, 1L);
        UninstallDeploymentElementOperation mockedUninstallOp = mock(UninstallDeploymentElementOperation.class);

        PowerMockito.whenNew(UninstallDeploymentElementOperation.class).withAnyArguments()
                .thenReturn(mockedUninstallOp);

        testFactory.createDeploymentElementOperation(uninstallDeploymentElement, localFile, installFolder);

        PowerMockito.verifyNew(UninstallDeploymentElementOperation.class)
                .withArguments(eq(uninstallDeploymentElement), eq(installFolder), eq(mockedFileManager),
                        eq(mockedOperationConfirmationProcessor));
    }
}