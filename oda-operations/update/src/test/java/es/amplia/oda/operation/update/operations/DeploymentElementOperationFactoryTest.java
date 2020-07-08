package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Collections;
import java.io.File;

import static es.amplia.oda.operation.api.OperationUpdate.*;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;

@RunWith(PowerMockRunner.class)
@PrepareForTest(DeploymentElementOperationFactory.class)
public class DeploymentElementOperationFactoryTest {

    private static final String LOCAL_FILE_JAR = "path/to/local/file.jar";
    private static final String INSTALL_FOLDER = "path/to/install/folder";
    private static final String PATH_TO_RULES = "path/to/rules/files";

    @Mock
    private FileManager mockedFileManager;
    @Mock
    private OperationConfirmationProcessor mockedOperationConfirmationProcessor;
    @InjectMocks
    private DeploymentElementOperationFactory testFactory;



    @After
    public void cleanUp() {
        StringBuilder path = new StringBuilder(PATH_TO_RULES);
        do {
            File testFile = new File(PATH_TO_RULES);
            if (testFile.exists()) {
                testFile.delete();
            }
            String[] dirs = path.toString().split("/");
            int max = dirs.length - 1;
            path = new StringBuilder();
            for (int i = 0; i < max; i++) {
                path.append(dirs[i]).append("/");
            }
        } while (!path.toString().equals(""));
    }

    @Test
    public void testCreateInstallDeploymentElementOperation() throws Exception {
        DeploymentElement installDeploymentElement =
                new DeploymentElement("","", DeploymentElementType.SOFTWARE, "", "", 1L,
                        DeploymentElementOperationType.INSTALL, Collections.EMPTY_LIST,
                        0L, "0.0.9", DeploymentElementOption.MANDATORY);
        InstallDeploymentElementOperation mockedInstallOp = mock(InstallDeploymentElementOperation.class);

        PowerMockito.whenNew(InstallDeploymentElementOperation.class).withAnyArguments().thenReturn(mockedInstallOp);

        testFactory.createDeploymentElementOperation(installDeploymentElement, LOCAL_FILE_JAR, INSTALL_FOLDER, PATH_TO_RULES);

        PowerMockito.verifyNew(InstallDeploymentElementOperation.class)
                .withArguments(eq(installDeploymentElement), eq(LOCAL_FILE_JAR), eq(INSTALL_FOLDER), eq(mockedFileManager),
                        eq(mockedOperationConfirmationProcessor));
    }

    @Test
    public void testCreateUpgradeDeploymentElementOperation() throws Exception {
        DeploymentElement upgradeDeploymentElement =
                new DeploymentElement("","", DeploymentElementType.CONFIGURATION, "", "", 1L,
                        DeploymentElementOperationType.UPGRADE,Collections.EMPTY_LIST,
                        0L, "0.0.9", DeploymentElementOption.OPTIONAL);
        UpgradeDeploymentElementOperation mockedUpgradeOp = mock(UpgradeDeploymentElementOperation.class);

        PowerMockito.whenNew(UpgradeDeploymentElementOperation.class).withAnyArguments().thenReturn(mockedUpgradeOp);

        testFactory.createDeploymentElementOperation(upgradeDeploymentElement, LOCAL_FILE_JAR, INSTALL_FOLDER, PATH_TO_RULES);

        PowerMockito.verifyNew(UpgradeDeploymentElementOperation.class)
                .withArguments(eq(upgradeDeploymentElement), eq(LOCAL_FILE_JAR), eq(INSTALL_FOLDER), eq(mockedFileManager),
                        eq(mockedOperationConfirmationProcessor));
    }

    @Test
    public void testCreateDeleteDeploymentElementOperation() throws Exception {
        DeploymentElement uninstallDeploymentElement =
                new DeploymentElement("","", DeploymentElementType.SOFTWARE, "", "", 1L,
                        DeploymentElementOperationType.UNINSTALL,Collections.EMPTY_LIST,
                        0L, "0.0.9", DeploymentElementOption.OPTIONAL);
        UninstallDeploymentElementOperation mockedUninstallOp = mock(UninstallDeploymentElementOperation.class);

        PowerMockito.whenNew(UninstallDeploymentElementOperation.class).withAnyArguments()
                .thenReturn(mockedUninstallOp);

        testFactory.createDeploymentElementOperation(uninstallDeploymentElement, LOCAL_FILE_JAR, INSTALL_FOLDER, PATH_TO_RULES);

        PowerMockito.verifyNew(UninstallDeploymentElementOperation.class)
                .withArguments(eq(uninstallDeploymentElement), eq(INSTALL_FOLDER), eq(mockedFileManager),
                        eq(mockedOperationConfirmationProcessor));
    }
}