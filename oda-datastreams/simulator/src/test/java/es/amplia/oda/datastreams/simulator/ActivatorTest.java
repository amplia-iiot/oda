package es.amplia.oda.datastreams.simulator;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.simulator.configuration.SimulatedDatastreamsConfigurationHandler;
import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsGetterFactory;

import es.amplia.oda.datastreams.simulator.internal.SimulatedDatastreamsSetterFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private SimulatedDatastreamsGetterFactory mockedGetterFactory;
    @Mock
    private SimulatedDatastreamsSetterFactory mockedSetterFactory;
    @Mock
    private ServiceRegistrationManagerOsgi<DatastreamsGetter> mockedRegistrationGetterManager;
    @Mock
    private ServiceRegistrationManagerOsgi<DatastreamsSetter> mockedRegistrationSetterManager;
    @Mock
    private SimulatedDatastreamsManager mockedDatastreamsManager;
    @Mock
    private SimulatedDatastreamsConfigurationHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigBundle;


    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(SimulatedDatastreamsGetterFactory.class).withAnyArguments().thenReturn(mockedGetterFactory);
        PowerMockito.whenNew(SimulatedDatastreamsSetterFactory.class).withAnyArguments().thenReturn(mockedSetterFactory);
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class).withArguments(any(), eq(DatastreamsGetter.class))
                .thenReturn(mockedRegistrationGetterManager);
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class).withArguments(any(), eq(DatastreamsSetter.class))
                .thenReturn(mockedRegistrationSetterManager);
        PowerMockito.whenNew(SimulatedDatastreamsManager.class).withAnyArguments().thenReturn(mockedDatastreamsManager);
        PowerMockito.whenNew(SimulatedDatastreamsConfigurationHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(SimulatedDatastreamsGetterFactory.class).withNoArguments();
        PowerMockito.verifyNew(SimulatedDatastreamsSetterFactory.class).withNoArguments();
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsSetter.class));
        PowerMockito.verifyNew(SimulatedDatastreamsManager.class)
                .withArguments(eq(mockedGetterFactory), eq(mockedSetterFactory), eq(mockedRegistrationGetterManager), eq(mockedRegistrationSetterManager));
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