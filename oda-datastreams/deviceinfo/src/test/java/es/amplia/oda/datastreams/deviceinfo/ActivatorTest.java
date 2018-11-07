package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.CommandProcessorImpl;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfigurationHandler;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private final Activator testActivator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private CommandProcessorImpl mockedCommandProcessor;
    @Mock
    private DeviceInfoDatastreamsGetter mockedDeviceDatastreamsGetter;
    @Mock
    private DeviceInfoConfigurationHandler mockedConfigHandler;
    @Mock
    private ConfigurableBundle mockedConfigBundle;
    @Mock
    private DatastreamsGetter datastreamsGetterForDeviceId;
    @Mock
    private DatastreamsGetter datastreamsGetterForSerialNumber;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedDatastreamsGetterRegistrationForSerialNumber;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedDatastreamsGetterRegistrationForDeviceId;
    @Mock
    private ServiceRegistration<DeviceInfoProvider> mockedDeviceIdProviderRegistration;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(CommandProcessorImpl.class).withAnyArguments().thenReturn(mockedCommandProcessor);
        PowerMockito.whenNew(DeviceInfoDatastreamsGetter.class).withAnyArguments()
                .thenReturn(mockedDeviceDatastreamsGetter);
        when(mockedDeviceDatastreamsGetter.getDatastreamsGetterForDeviceId()).thenReturn(datastreamsGetterForDeviceId);
        when(mockedDeviceDatastreamsGetter.getDatastreamsGetterForSerialNumber()).thenReturn(datastreamsGetterForSerialNumber);
        PowerMockito.whenNew(DeviceInfoConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundle.class).withAnyArguments().thenReturn(mockedConfigBundle);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(DeviceInfoDatastreamsGetter.class).withArguments(mockedCommandProcessor);
        PowerMockito.verifyNew(DeviceInfoConfigurationHandler.class).withArguments(eq(mockedDeviceDatastreamsGetter));
        PowerMockito.verifyNew(ConfigurableBundle.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        verify(mockedContext).registerService(eq(DeviceInfoProvider.class), eq(mockedDeviceDatastreamsGetter), any());
        verify(mockedContext).registerService(eq(DatastreamsGetter.class), eq(datastreamsGetterForDeviceId), any());
        verify(mockedContext).registerService(eq(DatastreamsGetter.class), eq(datastreamsGetterForSerialNumber), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigBundle);
        Whitebox.setInternalState(testActivator, "deviceIdProviderRegistration", mockedDeviceIdProviderRegistration);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForDeviceId",
                mockedDatastreamsGetterRegistrationForDeviceId);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForSerialNumber",
                mockedDatastreamsGetterRegistrationForSerialNumber);

        testActivator.stop(mockedContext);

        verify(mockedDatastreamsGetterRegistrationForSerialNumber).unregister();
        verify(mockedDatastreamsGetterRegistrationForDeviceId).unregister();
        verify(mockedDeviceIdProviderRegistration).unregister();
        verify(mockedConfigBundle).close();
    }
}