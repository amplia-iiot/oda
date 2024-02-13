package es.amplia.oda.datastreams.modbus;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.modbus.configuration.ModbusDatastreamsConfigurationUpdateHandler;
import es.amplia.oda.datastreams.modbus.internal.ModbusDatastreamsFactoryImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.any;
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
    private ModbusConnectionsFinder mockedConnectionsFinder;
    @Mock
    private ModbusDatastreamsFactoryImpl mockedFactory;
    @Mock
    private ServiceRegistrationManagerOsgi mockedRegistrationManager;
    @Mock
    private ModbusDatastreamsManager mockedModbusDatastreamsManager;
    @Mock
    private ModbusDatastreamsConfigurationUpdateHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;
    @Mock
    private ServiceListenerBundle<ModbusMaster> mockedModbusMasterListener;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(ModbusConnectionsFinder.class).withAnyArguments().thenReturn(mockedConnectionsFinder);
        PowerMockito.whenNew(ModbusDatastreamsFactoryImpl.class).withAnyArguments().thenReturn(mockedFactory);
        PowerMockito.whenNew(ServiceRegistrationManagerOsgi.class).withAnyArguments()
                .thenReturn(mockedRegistrationManager);
        PowerMockito.whenNew(ModbusDatastreamsManager.class).withAnyArguments()
                .thenReturn(mockedModbusDatastreamsManager);
        PowerMockito.whenNew(ModbusDatastreamsConfigurationUpdateHandler.class).withAnyArguments()
                .thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
        PowerMockito.whenNew(ServiceListenerBundle.class).withAnyArguments().thenReturn(mockedModbusMasterListener);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(ModbusDatastreamsFactoryImpl.class).withArguments(eq(mockedConnectionsFinder));
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(ServiceRegistrationManagerOsgi.class)
                .withArguments(eq(mockedContext), eq(DatastreamsGetter.class));
        PowerMockito.verifyNew(ModbusDatastreamsManager.class)
                .withArguments(eq(mockedFactory), eq(mockedRegistrationManager), eq(mockedRegistrationManager));
        PowerMockito.verifyNew(ModbusDatastreamsConfigurationUpdateHandler.class)
                .withArguments(eq(mockedModbusDatastreamsManager));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        PowerMockito.verifyNew(ServiceListenerBundle.class)
                .withArguments(eq(mockedContext), eq(ModbusMaster.class), any());

        verify(mockedConnectionsFinder).connect();
    }

    @Test
    public void testOnServiceChanged() {
        Whitebox.setInternalState(testActivator, "modbusConnectionsFinder", mockedConnectionsFinder);

        testActivator.onServiceChanged();

        verify(mockedConnectionsFinder).connect();
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "modbusConnectionsFinder", mockedConnectionsFinder);
        Whitebox.setInternalState(testActivator, "modbusDatastreamsManager", mockedModbusDatastreamsManager);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "modbusMasterListenerBundle", mockedModbusMasterListener);

        testActivator.stop(mockedContext);

        verify(mockedModbusMasterListener).close();
        verify(mockedConfigurableBundle).close();
        verify(mockedModbusDatastreamsManager).close();
        verify(mockedConnectionsFinder).close();
    }
}