package es.amplia.oda.hardware.modbus;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.hardware.modbus.Activator;
import es.amplia.oda.hardware.modbus.configuration.ModbusMasterConfigurationUpdateHandler;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterFactory;
import es.amplia.oda.hardware.modbus.internal.ModbusMasterManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceRegistrationManagerOsgi<ModbusMaster> mockedRegistrationManager;
    @Mock
    private ModbusMasterManager mockedModbusMasterManager;
    @Mock
    private ModbusMasterFactory mockedFactory;
    @Mock
    private ModbusMasterConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class).withAnyArguments()
                .thenReturn(mockedRegistrationManager);
        PowerMockito.whenNew(ModbusMasterManager.class).withAnyArguments().thenReturn(mockedModbusMasterManager);
        PowerMockito.whenNew(ModbusMasterFactory.class).withAnyArguments().thenReturn(mockedFactory);
        PowerMockito.whenNew(ModbusMasterConfigurationUpdateHandler.class)
                .withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class).withArguments(eq(mockedContext), eq(ModbusMaster.class));
        PowerMockito.verifyNew(ModbusMasterManager.class).withArguments(eq(mockedRegistrationManager));
        PowerMockito.verifyNew(ModbusMasterFactory.class).withNoArguments();
        PowerMockito.verifyNew(ModbusMasterConfigurationUpdateHandler.class)
                .withArguments(eq(mockedModbusMasterManager), eq(mockedFactory));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "modbusMasterManager", mockedModbusMasterManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);

        testActivator.stop(mockedContext);

        verify(mockedModbusMasterManager).close();
        verify(mockedConfigurableBundle).close();
    }
}