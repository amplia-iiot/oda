package es.amplia.oda.hardware.comms;

import es.amplia.oda.core.commons.utils.CommandProcessorImpl;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ScriptsLoaderImpl;
import es.amplia.oda.hardware.comms.configuration.CommsConfigurationUpdateHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ScriptsLoaderImpl mockedScriptsLoader;
    @Mock
    private CommsManagerImpl mockedCommsManager;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;

    @Test
    public void testStart() throws Exception {
        List<List<?>> scriptsLoaderArgs = new ArrayList<>();
        List<List<?>> commsManagerArgs = new ArrayList<>();
        List<List<?>> configHandlerArgs = new ArrayList<>();
        List<List<?>> configBundleArgs = new ArrayList<>();

        try (MockedConstruction<CommandProcessorImpl> commandProcessorCons = mockConstruction(CommandProcessorImpl.class);
             MockedConstruction<ScriptsLoaderImpl> scriptsLoaderCons = mockConstruction(ScriptsLoaderImpl.class,
                     (mock, mctx) -> scriptsLoaderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<CommsManagerImpl> commsManagerCons = mockConstruction(CommsManagerImpl.class,
                     (mock, mctx) -> commsManagerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<CommsConfigurationUpdateHandler> configHandlerCons = mockConstruction(CommsConfigurationUpdateHandler.class,
                     (mock, mctx) -> configHandlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configBundleCons = mockConstruction(ConfigurableBundleImpl.class,
                     (mock, mctx) -> configBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, commandProcessorCons.constructed().size());
            assertEquals(1, scriptsLoaderCons.constructed().size());
            assertEquals(1, commsManagerCons.constructed().size());
            assertEquals(1, configHandlerCons.constructed().size());
            assertEquals(1, configBundleCons.constructed().size());
            assertEquals(commandProcessorCons.constructed().get(0), scriptsLoaderArgs.get(0).get(0));
            assertEquals(commandProcessorCons.constructed().get(0), commsManagerArgs.get(0).get(0));
            assertEquals(scriptsLoaderCons.constructed().get(0), configHandlerArgs.get(0).get(0));
            assertEquals(commsManagerCons.constructed().get(0), configHandlerArgs.get(0).get(1));
            assertEquals(mockedContext, configBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configBundleArgs.get(0).get(1));
        }
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
