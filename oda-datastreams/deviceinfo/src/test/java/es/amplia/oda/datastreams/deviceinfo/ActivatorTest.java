package es.amplia.oda.datastreams.deviceinfo;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DeviceInfoProvider;
import es.amplia.oda.core.commons.utils.ConfigurableBundleNotifierService;
import es.amplia.oda.datastreams.deviceinfo.configuration.DeviceInfoConfigurationHandler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(PowerMockRunner.class)
@PrepareForTest(Activator.class)
public class ActivatorTest {

    private static final String TEST_BUNDLE_NAME = "testBundle";

    private Activator testActivator;

    @Mock
    private BundleContext mockedContext;
    @Mock
    private DeviceInfoDatastreamsGetter mockedDeviceDatastreamsGetter;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedDatastreamsGetterRegistrationForSerialNumber;
    @Mock
    private ServiceRegistration<DatastreamsGetter> mockedDatastreamsGetterRegistrationForDeviceId;
    @Mock
    private ServiceRegistration<DeviceInfoProvider> mockedDeviceIdProviderRegistration;
    @Mock
    private DeviceInfoConfigurationHandler mockedConfigHandler;
    @Mock
    private ServiceTracker<EventAdmin, EventAdmin> mockedEventAdminServiceTracker;
    @Mock
    private EventAdmin mockedEventAdmin;
    @Mock
    private ConfigurableBundleNotifierService mockedConfigService;
    @Mock
    private ServiceRegistration<ManagedService> mockedConfigServiceRegistration;
    @Mock
    private DatastreamsGetter datastreamsGetterForDeviceId;
    @Mock
    private DatastreamsGetter datastreamsGetterForSerialNumber;

    @Before
    public void setUp() {
        testActivator = new Activator();

        Bundle mockedBundle = mock(Bundle.class);
        when(mockedContext.getBundle()).thenReturn(mockedBundle);
        when(mockedBundle.getSymbolicName()).thenReturn(TEST_BUNDLE_NAME);
        
        when(mockedDeviceDatastreamsGetter.getDatastreamsGetterForDeviceId()).thenReturn(datastreamsGetterForDeviceId);
        when(mockedDeviceDatastreamsGetter.getDatastreamsGetterForSerialNumber()).thenReturn(datastreamsGetterForSerialNumber);
    }

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(DeviceInfoDatastreamsGetter.class)
                .withNoArguments().thenReturn(mockedDeviceDatastreamsGetter);
        PowerMockito.whenNew(DeviceInfoConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ServiceTracker.class)
                .withParameterTypes(BundleContext.class, Class.class, ServiceTrackerCustomizer.class)
                .withArguments(any(BundleContext.class), eq(EventAdmin.class), any())
                .thenReturn(mockedEventAdminServiceTracker);
        when(mockedEventAdminServiceTracker.getService()).thenReturn(mockedEventAdmin);
        PowerMockito.whenNew(ConfigurableBundleNotifierService.class)
                .withAnyArguments().thenReturn(mockedConfigService);

        testActivator.start(mockedContext);

        PowerMockito.whenNew(DeviceInfoDatastreamsGetter.class)
                .withNoArguments().thenReturn(mockedDeviceDatastreamsGetter);
        verify(mockedContext).registerService(eq(DatastreamsGetter.class), eq(datastreamsGetterForDeviceId), any());
        verify(mockedContext).registerService(eq(DatastreamsGetter.class), eq(datastreamsGetterForSerialNumber), any());
        verify(mockedContext).registerService(eq(DeviceInfoProvider.class), eq(mockedDeviceDatastreamsGetter), any());

        PowerMockito.verifyNew(DeviceInfoConfigurationHandler.class).withArguments(eq(mockedDeviceDatastreamsGetter));
        PowerMockito.verifyNew(ServiceTracker.class).withArguments(eq(mockedContext), eq(EventAdmin.class), any());
        verify(mockedEventAdminServiceTracker).open();
        PowerMockito.verifyNew(ConfigurableBundleNotifierService.class)
                .withArguments(eq(TEST_BUNDLE_NAME), eq(mockedConfigHandler), eq(mockedEventAdmin), any(List.class));
        verify(mockedContext).registerService(eq(ManagedService.class), eq(mockedConfigService), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForSerialNumber", mockedDatastreamsGetterRegistrationForSerialNumber);
        Whitebox.setInternalState(testActivator, "datastreamsGetterRegistrationForDeviceId", mockedDatastreamsGetterRegistrationForDeviceId);
        Whitebox.setInternalState(testActivator, "deviceIdProviderRegistration", mockedDeviceIdProviderRegistration);
        Whitebox.setInternalState(testActivator, "configServiceRegistration", mockedConfigServiceRegistration);
        Whitebox.setInternalState(testActivator, "eventAdminServiceTracker", mockedEventAdminServiceTracker);

        testActivator.stop(mockedContext);

        verify(mockedDatastreamsGetterRegistrationForSerialNumber).unregister();
        verify(mockedDatastreamsGetterRegistrationForDeviceId).unregister();
        verify(mockedDeviceIdProviderRegistration).unregister();
        verify(mockedConfigServiceRegistration).unregister();
        verify(mockedEventAdminServiceTracker).close();
    }
}