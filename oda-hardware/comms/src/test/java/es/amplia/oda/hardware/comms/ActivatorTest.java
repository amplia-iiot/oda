package es.amplia.oda.hardware.comms;

import es.amplia.oda.core.commons.utils.CommandProcessorImpl;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ScriptsLoaderImpl;
import es.amplia.oda.hardware.comms.configuration.CommsConfigurationUpdateHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;


import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private CommandProcessorImpl mockedCommandProcessor;
    @Mock
    private ScriptsLoaderImpl mockedScriptsLoader;
    @Mock
    private ResourceManagerImpl mockedResourceManager;
    @Mock
    private CommsManagerImpl mockedCommsManager;
    @Mock
    private CommsConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(CommandProcessorImpl.class).withNoArguments().thenReturn(mockedCommandProcessor);
        PowerMockito.whenNew(ScriptsLoaderImpl.class).withAnyArguments().thenReturn(mockedScriptsLoader);
        PowerMockito.whenNew(CommsManagerImpl.class).withAnyArguments().thenReturn(mockedCommsManager);
        PowerMockito.whenNew(CommsConfigurationUpdateHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(CommandProcessorImpl.class).withNoArguments();
        PowerMockito.verifyNew(ScriptsLoaderImpl.class).withArguments(eq(mockedCommandProcessor));
        PowerMockito.verifyNew(CommsManagerImpl.class).withArguments(eq(mockedCommandProcessor));
        PowerMockito.verifyNew(CommsConfigurationUpdateHandler.class).withArguments(eq(mockedScriptsLoader), eq(mockedCommsManager));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "scriptsLoader", mockedScriptsLoader);
        Whitebox.setInternalState(testActivator, "commsManager", mockedCommsManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);

        testActivator.stop(mockedContext);

        verify(mockedScriptsLoader).close();
        verify(mockedCommsManager).close();
        verify(mockedConfigBundle).close();
    }
}