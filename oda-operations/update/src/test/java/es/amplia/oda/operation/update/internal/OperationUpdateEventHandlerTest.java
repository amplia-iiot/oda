package es.amplia.oda.operation.update.internal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static es.amplia.oda.operation.api.OperationUpdate.*;
import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElementType.CONFIGURATION;
import static es.amplia.oda.operation.api.OperationUpdate.DeploymentElementType.SOFTWARE;
import static es.amplia.oda.operation.update.internal.OperationUpdateEventHandler.DELETE_CONFIGURATION_EVENT;
import static es.amplia.oda.operation.update.internal.OperationUpdateEventHandler.INSTALL_BUNDLE_EVENT;
import static es.amplia.oda.operation.update.internal.OperationUpdateEventHandler.OPERATION_TIMEOUT;
import static es.amplia.oda.operation.update.internal.OperationUpdateEventHandler.UNINSTALL_BUNDLE_EVENT;
import static es.amplia.oda.operation.update.internal.OperationUpdateEventHandler.UPDATE_CONFIGURATION_EVENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class OperationUpdateEventHandlerTest {

    private static final String TEST_BUNDLE = "testBundle";
    private static final String TEST_BUNDLE_TO_OPERATE = "test3";
    private static final String TEST_VERSION_TO_OPERATE = "3.0.0";
    private static final String WAITING_FOR_EVENT_FIELD_NAME = "waitingForEvent";
    private static final String WAITING_FOR_BUNDLE_NAME_FIELD_NAME = "waitingForBundleName";
    private static final String ACTIVE_LATCH_FIELD_NAME = "activeLatch";
    private static final String TEST_NAME_1 = "test1";
    private static final String TEST_VERSION_1 = "1.0.0";
    private static final String TEST_NAME_2 = "test2";
    private static final String TEST_VERSION_2 = "2.2.2";

    @Mock
    private BundleContext mockedContext;
    @InjectMocks
    private OperationUpdateEventHandler testHandler;

    @Mock
    private DeploymentElement mockedDeploymentElement;

    @Test
    public void testHandleEventWaitingBundle() {
        String testEventTopic = INSTALL_BUNDLE_EVENT;
        Event mockedEvent = mock(Event.class);
        CountDownLatch mockedCountDownLatch = mock(CountDownLatch.class);

        when(mockedEvent.getTopic()).thenReturn(testEventTopic);
        when(mockedEvent.getProperty(EventConstants.BUNDLE_SYMBOLICNAME)).thenReturn(TEST_BUNDLE);
        Whitebox.setInternalState(testHandler, WAITING_FOR_EVENT_FIELD_NAME, testEventTopic);
        Whitebox.setInternalState(testHandler, WAITING_FOR_BUNDLE_NAME_FIELD_NAME, TEST_BUNDLE);
        Whitebox.setInternalState(testHandler, ACTIVE_LATCH_FIELD_NAME, mockedCountDownLatch);

        testHandler.handleEvent(mockedEvent);

        verify(mockedCountDownLatch).countDown();
    }

    @Test
    public void testHandleEventBundleWithDifferentName() {
        String testEventTopic = INSTALL_BUNDLE_EVENT;
        Event mockedEvent = mock(Event.class);
        CountDownLatch mockedCountDownLatch = mock(CountDownLatch.class);

        when(mockedEvent.getTopic()).thenReturn(testEventTopic);
        when(mockedEvent.getProperty(EventConstants.BUNDLE_SYMBOLICNAME)).thenReturn("anotherBundle");
        Whitebox.setInternalState(testHandler, WAITING_FOR_EVENT_FIELD_NAME, testEventTopic);
        Whitebox.setInternalState(testHandler, WAITING_FOR_BUNDLE_NAME_FIELD_NAME, TEST_BUNDLE);
        Whitebox.setInternalState(testHandler, ACTIVE_LATCH_FIELD_NAME, mockedCountDownLatch);

        testHandler.handleEvent(mockedEvent);

        verify(mockedCountDownLatch, never()).countDown();
    }

    @Test
    public void testHandleEventDifferentTopic() {
        Event mockedEvent = mock(Event.class);
        CountDownLatch mockedCountDownLatch = mock(CountDownLatch.class);

        when(mockedEvent.getTopic()).thenReturn(INSTALL_BUNDLE_EVENT);
        Whitebox.setInternalState(testHandler, WAITING_FOR_EVENT_FIELD_NAME, UNINSTALL_BUNDLE_EVENT);
        Whitebox.setInternalState(testHandler, WAITING_FOR_BUNDLE_NAME_FIELD_NAME, TEST_BUNDLE);
        Whitebox.setInternalState(testHandler, ACTIVE_LATCH_FIELD_NAME, mockedCountDownLatch);

        testHandler.handleEvent(mockedEvent);

        verify(mockedCountDownLatch, never()).countDown();
    }

    @Test
    public void testWaitForConfirmationAlreadyInstalled() throws Exception {
        Bundle mockedBundle1 = mock(Bundle.class);
        Bundle mockedBundle2 = mock(Bundle.class);
        Bundle mockedBundle3 = mock(Bundle.class);
        Bundle[] bundles = new Bundle[] { mockedBundle1, mockedBundle2, mockedBundle3};

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.INSTALL);
        when(mockedContext.getBundles()).thenReturn(bundles);
        when(mockedBundle1.getSymbolicName()).thenReturn(TEST_NAME_1);
        when(mockedBundle1.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_1));
        when(mockedBundle1.getState()).thenReturn(Bundle.ACTIVE);
        when(mockedBundle2.getSymbolicName()).thenReturn(TEST_NAME_2);
        when(mockedBundle2.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_2));
        when(mockedBundle2.getState()).thenReturn(Bundle.RESOLVED);
        when(mockedBundle3.getSymbolicName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedBundle3.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_TO_OPERATE));
        when(mockedBundle3.getState()).thenReturn(Bundle.ACTIVE);

        try (MockedConstruction<CountDownLatch> latchCons = mockConstruction(CountDownLatch.class)) {
            testHandler.waitForConfirmation(mockedDeploymentElement);

            assertEquals(0, latchCons.constructed().size());
        }
    }

    @Test
    public void testWaitForConfirmationAlreadyUpgrade() throws Exception {
        Bundle mockedBundle1 = mock(Bundle.class);
        Bundle mockedBundle2 = mock(Bundle.class);
        Bundle mockedBundle3 = mock(Bundle.class);
        Bundle[] bundles = new Bundle[] { mockedBundle1, mockedBundle2, mockedBundle3};

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.UPGRADE);
        when(mockedContext.getBundles()).thenReturn(bundles);
        when(mockedBundle1.getSymbolicName()).thenReturn(TEST_NAME_1);
        when(mockedBundle1.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_1));
        when(mockedBundle1.getState()).thenReturn(Bundle.ACTIVE);
        when(mockedBundle2.getSymbolicName()).thenReturn(TEST_NAME_2);
        when(mockedBundle2.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_2));
        when(mockedBundle2.getState()).thenReturn(Bundle.RESOLVED);
        when(mockedBundle3.getSymbolicName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedBundle3.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_TO_OPERATE));
        when(mockedBundle3.getState()).thenReturn(Bundle.ACTIVE);

        try (MockedConstruction<CountDownLatch> latchCons = mockConstruction(CountDownLatch.class)) {
            testHandler.waitForConfirmation(mockedDeploymentElement);

            assertEquals(0, latchCons.constructed().size());
        }
    }

    @Test
    public void testWaitForConfirmationAlreadyUninstall() throws Exception {
        Bundle mockedBundle1 = mock(Bundle.class);
        Bundle mockedBundle2 = mock(Bundle.class);
        Bundle[] bundles = new Bundle[] { mockedBundle1, mockedBundle2 };

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.UNINSTALL);
        when(mockedContext.getBundles()).thenReturn(bundles);
        when(mockedBundle1.getSymbolicName()).thenReturn(TEST_NAME_1);
        when(mockedBundle1.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_1));
        when(mockedBundle1.getState()).thenReturn(Bundle.ACTIVE);
        when(mockedBundle2.getSymbolicName()).thenReturn(TEST_NAME_2);
        when(mockedBundle2.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_2));
        when(mockedBundle2.getState()).thenReturn(Bundle.RESOLVED);

        try (MockedConstruction<CountDownLatch> latchCons = mockConstruction(CountDownLatch.class)) {
            testHandler.waitForConfirmation(mockedDeploymentElement);

            assertEquals(0, latchCons.constructed().size());
        }
    }

    @Test
    public void testWaitForConfirmationAlreadyOrphanConfiguration() throws Exception {
        Bundle mockedBundle1 = mock(Bundle.class);
        Bundle mockedBundle2 = mock(Bundle.class);
        Bundle[] bundles = new Bundle[] { mockedBundle1, mockedBundle2 };

        when(mockedDeploymentElement.getName()).thenReturn("uninstalledBundle");
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_1);
        when(mockedDeploymentElement.getType()).thenReturn(CONFIGURATION);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.INSTALL);
        when(mockedContext.getBundles()).thenReturn(bundles);
        when(mockedBundle1.getSymbolicName()).thenReturn(TEST_NAME_1);
        when(mockedBundle1.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_1));
        when(mockedBundle1.getState()).thenReturn(Bundle.ACTIVE);
        when(mockedBundle2.getSymbolicName()).thenReturn(TEST_NAME_2);
        when(mockedBundle2.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_2));
        when(mockedBundle2.getState()).thenReturn(Bundle.RESOLVED);

        try (MockedConstruction<CountDownLatch> latchCons = mockConstruction(CountDownLatch.class)) {
            testHandler.waitForConfirmation(mockedDeploymentElement);

            assertEquals(0, latchCons.constructed().size());
        }
    }

    @Test
    public void testWaitForConfirmationAlreadyUninstallDifferentVersionInstalled() throws Exception {
        Bundle mockedBundle1 = mock(Bundle.class);
        Bundle mockedBundle2 = mock(Bundle.class);
        Bundle mockedBundle3 = mock(Bundle.class);
        Bundle[] bundles = new Bundle[] { mockedBundle1, mockedBundle2, mockedBundle3 };

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.UNINSTALL);
        when(mockedContext.getBundles()).thenReturn(bundles);
        when(mockedBundle1.getSymbolicName()).thenReturn(TEST_NAME_1);
        when(mockedBundle1.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_1));
        when(mockedBundle1.getState()).thenReturn(Bundle.ACTIVE);
        when(mockedBundle2.getSymbolicName()).thenReturn(TEST_NAME_2);
        when(mockedBundle2.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_2));
        when(mockedBundle2.getState()).thenReturn(Bundle.RESOLVED);
        when(mockedBundle3.getSymbolicName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedBundle3.getVersion()).thenReturn(Version.parseVersion("2.0.0"));
        when(mockedBundle3.getState()).thenReturn(Bundle.ACTIVE);

        try (MockedConstruction<CountDownLatch> latchCons = mockConstruction(CountDownLatch.class)) {
            testHandler.waitForConfirmation(mockedDeploymentElement);

            assertEquals(0, latchCons.constructed().size());
        }
    }

    @Test
    public void testWaitForConfirmationInstallOperation() {
        Event mockedEvent = mock(Event.class);

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.INSTALL);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{});
        when(mockedEvent.getTopic()).thenReturn(INSTALL_BUNDLE_EVENT);
        when(mockedEvent.getProperty(eq(EventConstants.BUNDLE_SYMBOLICNAME))).thenReturn(TEST_BUNDLE_TO_OPERATE);

        ScheduledExecutorService scheduleSendEvent = Executors.newSingleThreadScheduledExecutor();
        scheduleSendEvent.schedule(() -> testHandler.handleEvent(mockedEvent), 1, TimeUnit.SECONDS);

        boolean confirmed = testHandler.waitForConfirmation(mockedDeploymentElement);

        scheduleSendEvent.shutdown();

        assertTrue(confirmed);
    }

    @Test
    public void testWaitForConfirmationUpgradeOperation() {
        Event mockedEvent = mock(Event.class);

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.UPGRADE);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{});
        when(mockedEvent.getTopic()).thenReturn(INSTALL_BUNDLE_EVENT);
        when(mockedEvent.getProperty(eq(EventConstants.BUNDLE_SYMBOLICNAME))).thenReturn(TEST_BUNDLE_TO_OPERATE);

        ScheduledExecutorService scheduleSendEvent = Executors.newSingleThreadScheduledExecutor();
        scheduleSendEvent.schedule(() -> testHandler.handleEvent(mockedEvent), 1, TimeUnit.SECONDS);

        boolean confirmed = testHandler.waitForConfirmation(mockedDeploymentElement);

        scheduleSendEvent.shutdown();

        assertTrue(confirmed);
    }

    @Test
    public void testWaitForConfirmationUninstallOperation() {
        Event mockedEvent = mock(Event.class);
        Bundle mockedBundleToUninstall = mock(Bundle.class);

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.UNINSTALL);
        when(mockedBundleToUninstall.getSymbolicName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedBundleToUninstall.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_TO_OPERATE));
        when(mockedBundleToUninstall.getState()).thenReturn(Bundle.ACTIVE);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{ mockedBundleToUninstall});
        when(mockedEvent.getTopic()).thenReturn(UNINSTALL_BUNDLE_EVENT);
        when(mockedEvent.getProperty(eq(EventConstants.BUNDLE_SYMBOLICNAME))).thenReturn(TEST_BUNDLE_TO_OPERATE);

        ScheduledExecutorService scheduleSendEvent = Executors.newSingleThreadScheduledExecutor();
        scheduleSendEvent.schedule(() -> testHandler.handleEvent(mockedEvent), 1, TimeUnit.SECONDS);

        boolean confirmed = testHandler.waitForConfirmation(mockedDeploymentElement);

        scheduleSendEvent.shutdown();

        assertTrue(confirmed);
    }

    @Test
    public void testWaitForConfirmationInstallConfigurationOperation() {
        Event mockedEvent = mock(Event.class);

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(DeploymentElementType.CONFIGURATION);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.INSTALL);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{});
        when(mockedEvent.getTopic()).thenReturn(UPDATE_CONFIGURATION_EVENT);
        when(mockedEvent.getProperty(eq(EventConstants.BUNDLE_SYMBOLICNAME))).thenReturn(TEST_BUNDLE_TO_OPERATE);

        ScheduledExecutorService scheduleSendEvent = Executors.newSingleThreadScheduledExecutor();
        scheduleSendEvent.schedule(() -> testHandler.handleEvent(mockedEvent), 1, TimeUnit.SECONDS);

        boolean confirmed = testHandler.waitForConfirmation(mockedDeploymentElement);

        scheduleSendEvent.shutdown();

        assertTrue(confirmed);
    }

    @Test
    public void testWaitForConfirmationUpgradeConfigurationOperation() {
        Event mockedEvent = mock(Event.class);

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(DeploymentElementType.CONFIGURATION);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.UPGRADE);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{});
        when(mockedEvent.getTopic()).thenReturn(UPDATE_CONFIGURATION_EVENT);
        when(mockedEvent.getProperty(eq(EventConstants.BUNDLE_SYMBOLICNAME))).thenReturn(TEST_BUNDLE_TO_OPERATE);

        ScheduledExecutorService scheduleSendEvent = Executors.newSingleThreadScheduledExecutor();
        scheduleSendEvent.schedule(() -> testHandler.handleEvent(mockedEvent), 1, TimeUnit.SECONDS);

        boolean confirmed = testHandler.waitForConfirmation(mockedDeploymentElement);

        scheduleSendEvent.shutdown();

        assertTrue(confirmed);
    }

    @Test
    public void testWaitForConfirmationUninstallConfigurationOperation() {
        Event mockedEvent = mock(Event.class);
        Bundle mockedBundleToUninstall = mock(Bundle.class);

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(DeploymentElementType.CONFIGURATION);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.UNINSTALL);
        when(mockedBundleToUninstall.getSymbolicName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedBundleToUninstall.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_TO_OPERATE));
        when(mockedBundleToUninstall.getState()).thenReturn(Bundle.ACTIVE);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{ mockedBundleToUninstall});
        when(mockedEvent.getTopic()).thenReturn(DELETE_CONFIGURATION_EVENT);
        when(mockedEvent.getProperty(eq(EventConstants.BUNDLE_SYMBOLICNAME))).thenReturn(TEST_BUNDLE_TO_OPERATE);

        ScheduledExecutorService scheduleSendEvent = Executors.newSingleThreadScheduledExecutor();
        scheduleSendEvent.schedule(() -> testHandler.handleEvent(mockedEvent), 1, TimeUnit.SECONDS);

        boolean confirmed = testHandler.waitForConfirmation(mockedDeploymentElement);

        scheduleSendEvent.shutdown();

        assertTrue(confirmed);
    }

    @Test
    public void testWaitForConfirmationTimeout() throws Exception {
        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.INSTALL);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{});

        // Mocked CountDownLatch await to not wait for timeout in test. Timeout is represented by false return value.
        List<List<?>> latchArgs = new ArrayList<>();
        try (MockedConstruction<CountDownLatch> latchCons = mockConstruction(CountDownLatch.class,
                (mock, mctx) -> {
                    latchArgs.add(new ArrayList<>(mctx.arguments()));
                    when(mock.await(anyLong(), any(TimeUnit.class))).thenReturn(false);
                })) {

            boolean confirmed = testHandler.waitForConfirmation(mockedDeploymentElement);

            assertFalse(confirmed);
            assertEquals(1, latchCons.constructed().size());
            assertEquals(1, latchArgs.get(0).get(0));
            verify(latchCons.constructed().get(0)).await(eq(OPERATION_TIMEOUT), eq(TimeUnit.SECONDS));
        }
    }

    @Test
    public void testWaitForConfirmationUnhandledType() {
        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(DeploymentElementType.FIRMWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.INSTALL);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{});

        boolean confirmed = testHandler.waitForConfirmation(mockedDeploymentElement);

        assertFalse(confirmed);
    }

    @Test
    public void testWaitForRollbackConfirmationInstallOperation() {
        Event mockedEvent = mock(Event.class);

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.INSTALL);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{});
        when(mockedEvent.getTopic()).thenReturn(UNINSTALL_BUNDLE_EVENT);
        when(mockedEvent.getProperty(eq(EventConstants.BUNDLE_SYMBOLICNAME))).thenReturn(TEST_BUNDLE_TO_OPERATE);

        ScheduledExecutorService scheduleSendEvent = Executors.newSingleThreadScheduledExecutor();
        scheduleSendEvent.schedule(() -> testHandler.handleEvent(mockedEvent), 1, TimeUnit.SECONDS);

        boolean confirmed = testHandler.waitForRollbackConfirmation(mockedDeploymentElement);

        scheduleSendEvent.shutdown();

        assertTrue(confirmed);
    }

    @Test
    public void testWaitForRollbackConfirmationUpgradeOperation() {
        Event mockedEvent = mock(Event.class);

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.UPGRADE);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{});
        when(mockedEvent.getTopic()).thenReturn(INSTALL_BUNDLE_EVENT);
        when(mockedEvent.getProperty(eq(EventConstants.BUNDLE_SYMBOLICNAME))).thenReturn(TEST_BUNDLE_TO_OPERATE);

        ScheduledExecutorService scheduleSendEvent = Executors.newSingleThreadScheduledExecutor();
        scheduleSendEvent.schedule(() -> testHandler.handleEvent(mockedEvent), 1, TimeUnit.SECONDS);

        boolean confirmed = testHandler.waitForRollbackConfirmation(mockedDeploymentElement);

        scheduleSendEvent.shutdown();

        assertTrue(confirmed);
    }

    @Test
    public void testWaitForRollbackConfirmationUninstallOperation() {
        Event mockedEvent = mock(Event.class);
        Bundle mockedBundleToUninstall = mock(Bundle.class);

        when(mockedDeploymentElement.getName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedDeploymentElement.getVersion()).thenReturn(TEST_VERSION_TO_OPERATE);
        when(mockedDeploymentElement.getType()).thenReturn(SOFTWARE);
        when(mockedDeploymentElement.getOperation()).thenReturn(DeploymentElementOperationType.UNINSTALL);
        when(mockedBundleToUninstall.getSymbolicName()).thenReturn(TEST_BUNDLE_TO_OPERATE);
        when(mockedBundleToUninstall.getVersion()).thenReturn(Version.parseVersion(TEST_VERSION_TO_OPERATE));
        when(mockedBundleToUninstall.getState()).thenReturn(Bundle.ACTIVE);
        when(mockedContext.getBundles()).thenReturn(new Bundle[]{ mockedBundleToUninstall});
        when(mockedEvent.getTopic()).thenReturn(INSTALL_BUNDLE_EVENT);
        when(mockedEvent.getProperty(eq(EventConstants.BUNDLE_SYMBOLICNAME))).thenReturn(TEST_BUNDLE_TO_OPERATE);

        ScheduledExecutorService scheduleSendEvent = Executors.newSingleThreadScheduledExecutor();
        scheduleSendEvent.schedule(() -> testHandler.handleEvent(mockedEvent), 1, TimeUnit.SECONDS);

        boolean confirmed = testHandler.waitForRollbackConfirmation(mockedDeploymentElement);

        scheduleSendEvent.shutdown();

        assertTrue(confirmed);
    }
}
