package es.amplia.oda.subsystem.sshserver;

import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.subsystem.sshserver.configuration.SshConfigurationUpdateHandler;
import es.amplia.oda.subsystem.sshserver.internal.ConfigurablePasswordAuthenticatorImpl;
import es.amplia.oda.subsystem.sshserver.internal.SshCommandShell;

import org.apache.felix.service.command.CommandProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ActivatorTest {

    private static final String COMMAND_PROCESSOR_FIELD_NAME = "commandProcessor";
    private static final String CONFIG_HANDLER_FIELD_NAME = "configHandler";
    private static final String CONFIGURABLE_BUNDLE_FIELD_NAME = "configurableBundle";
    private static final String COMMAND_PROCESSOR_LISTENER_BUNDLE_FIELD_NAME = "commandProcessorListenerBundle";
    private static final String SSH_COMMAND_SHELL_FIELD_NAME = "sshCommandShell";

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private CommandProcessorProxy mockedCommandProcessor;
    @Mock
    private SshCommandShell mockedSshCommandShell;
    @Mock
    private SshConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;
    @Mock
    private ServiceListenerBundle<CommandProcessor> mockedServiceListener;


    @Test
    public void testStart() throws Exception {
        List<List<?>> commandProcessorArgs = new ArrayList<>();
        List<List<?>> passwordAuthenticatorArgs = new ArrayList<>();
        List<List<?>> sshCommandShellArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();
        List<List<?>> serviceListenerArgs = new ArrayList<>();

        try (MockedConstruction<CommandProcessorProxy> commandProcessorCons = mockConstruction(CommandProcessorProxy.class,
                     (mock, mctx) -> commandProcessorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurablePasswordAuthenticatorImpl> passwordAuthenticatorCons = mockConstruction(ConfigurablePasswordAuthenticatorImpl.class,
                     (mock, mctx) -> passwordAuthenticatorArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SshCommandShell> sshCommandShellCons = mockConstruction(SshCommandShell.class,
                     (mock, mctx) -> sshCommandShellArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SshConfigurationUpdateHandler> configHandlerCons = mockConstruction(SshConfigurationUpdateHandler.class,
                     (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons = mockConstruction(ConfigurableBundleImpl.class,
                     (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceListenerBundle> serviceListenerCons = mockConstruction(ServiceListenerBundle.class,
                     (mock, mctx) -> serviceListenerArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(Collections.singletonList(mockedContext), commandProcessorArgs.get(0));
            assertEquals(Collections.emptyList(), passwordAuthenticatorArgs.get(0));
            assertEquals(Arrays.asList(commandProcessorCons.constructed().get(0),
                    passwordAuthenticatorCons.constructed().get(0)), sshCommandShellArgs.get(0));
            assertEquals(Collections.singletonList(sshCommandShellCons.constructed().get(0)), configHandlerArgs.get(0));
            assertEquals(Arrays.asList(mockedContext, configHandlerCons.constructed().get(0)), configurableBundleArgs.get(0));
            assertEquals(1, serviceListenerCons.constructed().size());
            assertEquals(3, serviceListenerArgs.get(0).size());
            assertEquals(mockedContext, serviceListenerArgs.get(0).get(0));
            assertEquals(CommandProcessor.class, serviceListenerArgs.get(0).get(1));
            assertTrue(serviceListenerArgs.get(0).get(2) instanceof Runnable);
        }
    }

    @Test
    public void testOnServiceChanged() {
        Whitebox.setInternalState(testActivator, CONFIG_HANDLER_FIELD_NAME, mockedConfigHandler);

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedOIOExceptionCaught() {
        Whitebox.setInternalState(testActivator, CONFIG_HANDLER_FIELD_NAME, mockedConfigHandler);

        doThrow(new IllegalArgumentException()).when(mockedConfigHandler).applyConfiguration();

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, COMMAND_PROCESSOR_FIELD_NAME, mockedCommandProcessor);
        Whitebox.setInternalState(testActivator, CONFIG_HANDLER_FIELD_NAME, mockedConfigHandler);
        Whitebox.setInternalState(testActivator, CONFIGURABLE_BUNDLE_FIELD_NAME, mockedConfigBundle);
        Whitebox.setInternalState(testActivator, COMMAND_PROCESSOR_LISTENER_BUNDLE_FIELD_NAME, mockedServiceListener);
        Whitebox.setInternalState(testActivator, SSH_COMMAND_SHELL_FIELD_NAME, mockedSshCommandShell);

        testActivator.stop(mockedContext);

        verify(mockedServiceListener).close();
        verify(mockedConfigBundle).close();
        verify(mockedCommandProcessor).close();
        verify(mockedSshCommandShell).close();
    }
}
