package es.amplia.oda.operation.update.operations;

import es.amplia.oda.operation.update.FileManager;
import es.amplia.oda.operation.update.OperationConfirmationProcessor;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.File;

import static es.amplia.oda.operation.api.OperationUpdate.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DeploymentElementOperationFactoryTest {

    private static final String LOCAL_FILE_JAR = "path/to/local/file.jar";
    private static final String INSTALL_FOLDER = "path/to/install/folder";
    private static final String PATH_TO_RULES = "path/to/rules/files";
    private static final String PATH_TO_RULES_UTILS = "path/to/jslib";


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

        List<List<?>> installOpArgs = new ArrayList<>();
        try (MockedConstruction<InstallDeploymentElementOperation> installOpCons =
                     mockConstruction(InstallDeploymentElementOperation.class,
                             (mock, mctx) -> installOpArgs.add(new ArrayList<>(mctx.arguments())))) {

            testFactory.createDeploymentElementOperation(installDeploymentElement, LOCAL_FILE_JAR, INSTALL_FOLDER,
                    PATH_TO_RULES, PATH_TO_RULES_UTILS);

            assertEquals(1, installOpCons.constructed().size());
            assertEquals(installDeploymentElement, installOpArgs.get(0).get(0));
            assertEquals(LOCAL_FILE_JAR, installOpArgs.get(0).get(1));
            assertEquals(INSTALL_FOLDER, installOpArgs.get(0).get(2));
            assertEquals(mockedFileManager, installOpArgs.get(0).get(3));
            assertEquals(mockedOperationConfirmationProcessor, installOpArgs.get(0).get(4));
        }
    }

    @Test
    public void testCreateUpgradeDeploymentElementOperation() throws Exception {
        DeploymentElement upgradeDeploymentElement =
                new DeploymentElement("","", DeploymentElementType.CONFIGURATION, "", "", 1L,
                        DeploymentElementOperationType.UPGRADE,Collections.EMPTY_LIST,
                        0L, "0.0.9", DeploymentElementOption.OPTIONAL);

        List<List<?>> upgradeOpArgs = new ArrayList<>();
        try (MockedConstruction<UpgradeDeploymentElementOperation> upgradeOpCons =
                     mockConstruction(UpgradeDeploymentElementOperation.class,
                             (mock, mctx) -> upgradeOpArgs.add(new ArrayList<>(mctx.arguments())))) {

            testFactory.createDeploymentElementOperation(upgradeDeploymentElement, LOCAL_FILE_JAR, INSTALL_FOLDER,
                    PATH_TO_RULES, PATH_TO_RULES_UTILS);

            assertEquals(1, upgradeOpCons.constructed().size());
            assertEquals(upgradeDeploymentElement, upgradeOpArgs.get(0).get(0));
            assertEquals(LOCAL_FILE_JAR, upgradeOpArgs.get(0).get(1));
            assertEquals(INSTALL_FOLDER, upgradeOpArgs.get(0).get(2));
            assertEquals(mockedFileManager, upgradeOpArgs.get(0).get(3));
            assertEquals(mockedOperationConfirmationProcessor, upgradeOpArgs.get(0).get(4));
        }
    }

    @Test
    public void testCreateDeleteDeploymentElementOperation() throws Exception {
        DeploymentElement uninstallDeploymentElement =
                new DeploymentElement("","", DeploymentElementType.SOFTWARE, "", "", 1L,
                        DeploymentElementOperationType.UNINSTALL,Collections.EMPTY_LIST,
                        0L, "0.0.9", DeploymentElementOption.OPTIONAL);

        List<List<?>> uninstallOpArgs = new ArrayList<>();
        try (MockedConstruction<UninstallDeploymentElementOperation> uninstallOpCons =
                     mockConstruction(UninstallDeploymentElementOperation.class,
                             (mock, mctx) -> uninstallOpArgs.add(new ArrayList<>(mctx.arguments())))) {

            testFactory.createDeploymentElementOperation(uninstallDeploymentElement, LOCAL_FILE_JAR, INSTALL_FOLDER,
                    PATH_TO_RULES, PATH_TO_RULES_UTILS);

            assertEquals(1, uninstallOpCons.constructed().size());
            assertEquals(uninstallDeploymentElement, uninstallOpArgs.get(0).get(0));
            assertEquals(INSTALL_FOLDER, uninstallOpArgs.get(0).get(1));
            assertEquals(mockedFileManager, uninstallOpArgs.get(0).get(2));
            assertEquals(mockedOperationConfirmationProcessor, uninstallOpArgs.get(0).get(3));
        }
    }
}
