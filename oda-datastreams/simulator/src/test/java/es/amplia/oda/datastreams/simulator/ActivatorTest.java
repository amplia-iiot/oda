package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;

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
    private SimulatedDatastreamsGetterFactory mockedFactory;
    @Mock
    private ServiceRegistrationManagerOsgi<DatastreamsGetter> mockedRegistrationManager;
    @Mock
    private SimulatedDatastreamsManager mockedDatastreamsManager;
    @Mock
    private SimulatedDatastreamsConfigurationHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(SimulatedDatastreamsGetterFactory.class).withAnyArguments().thenReturn(mockedFactory);
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class).withAnyArguments()
                .thenReturn(mockedRegistrationManager);
        PowerMockito.whenNew(SimulatedDatastreamsManager.class).withAnyArguments().thenReturn(mockedDatastreamsManager);
        PowerMockito.whenNew(SimulatedDatastreamsConfigurationHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(SimulatedDatastreamsGetterFactory.class).withNoArguments();
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(SimulatedDatastreamsManager.class)
                .withArguments(eq(mockedFactory), eq(mockedRegistrationManager));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "datastreamsManager", mockedDatastreamsManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);

        testActivator.stop(mockedContext);

        verify(mockedConfigBundle).close();
        verify(mockedDatastreamsManager).close();
    }
}