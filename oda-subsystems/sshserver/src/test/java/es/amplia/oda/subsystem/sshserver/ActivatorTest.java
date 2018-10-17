package es.amplia.oda.subsystem.sshserver;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.subsystem.sshserver.configuration.SshConfigurationUpdateHandler;
import es.amplia.oda.subsystem.sshserver.internal.ConfigurablePasswordAuthenticatorImpl;
import es.amplia.oda.subsystem.sshserver.internal.SshCommandShell;

import org.apache.felix.service.command.CommandProcessor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.IOException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
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
    private ConfigurablePasswordAuthenticatorImpl mockedPasswordAuthenticator;
    @Mock
    private SshCommandShell mockedSshCommandShell;
    @Mock
    private SshConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundle mockedConfigBundle;
    @Mock
    private ServiceListenerBundle<CommandProcessor> mockedServiceListener;


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(CommandProcessorProxy.class).withAnyArguments().thenReturn(mockedCommandProcessor);
        PowerMockito.whenNew(ConfigurablePasswordAuthenticatorImpl.class).withAnyArguments()
                .thenReturn(mockedPasswordAuthenticator);
        PowerMockito.whenNew(SshCommandShell.class).withAnyArguments().thenReturn(mockedSshCommandShell);
        PowerMockito.whenNew(SshConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundle.class).withAnyArguments().thenReturn(mockedConfigBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedServiceListener);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(CommandProcessorProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ConfigurablePasswordAuthenticatorImpl.class).withNoArguments();
        PowerMockito.verifyNew(SshCommandShell.class)
                .withArguments(eq(mockedCommandProcessor), eq(mockedPasswordAuthenticator));
        PowerMockito.verifyNew(SshConfigurationUpdateHandler.class).withArguments(eq(mockedSshCommandShell));
        PowerMockito.verifyNew(ConfigurableBundle.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(CommandProcessor.class), any(Runnable.class));
    }

    @Test
    public void testOnServiceChanged() throws IOException {
        Whitebox.setInternalState(testActivator, CONFIG_HANDLER_FIELD_NAME, mockedConfigHandler);

        testActivator.onServiceChanged();

        verify(mockedConfigHandler).applyConfiguration();
    }

    @Test
    public void testOnServiceChangedOIOExceptionCaught() throws IOException {
        Whitebox.setInternalState(testActivator, CONFIG_HANDLER_FIELD_NAME, mockedConfigHandler);

        doThrow(new IOException()).when(mockedConfigHandler).applyConfiguration();

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