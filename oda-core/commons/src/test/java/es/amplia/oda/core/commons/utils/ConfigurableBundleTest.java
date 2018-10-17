package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.osgi.proxies.EventAdminProxy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ManagedService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.Hashtable;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ConfigurableBundle.class)
public class ConfigurableBundleTest {

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ConfigurationUpdateHandler mockedHandler;

    private ConfigurableBundle testConfigurableBundle;

    private String bundleName = "testBundle";
    @Mock
    private EventAdminProxy mockedEventAdmin;
    @Mock
    private ConfigurableBundleNotifierService mockedConfigService;
    @Mock
    private ServiceRegistration<ManagedService> mockedRegistration;

    @Before
    public void setUp() throws Exception {
        Bundle mockedBundle = mock(Bundle.class);

        when(mockedContext.getBundle()).thenReturn(mockedBundle);
        when(mockedBundle.getSymbolicName()).thenReturn(bundleName);
        PowerMockito.whenNew(EventAdminProxy.class).withAnyArguments().thenReturn(mockedEventAdmin);
        PowerMockito.whenNew(ConfigurableBundleNotifierService.class).withAnyArguments()
                .thenReturn(mockedConfigService);

        testConfigurableBundle = new ConfigurableBundle(mockedContext, mockedHandler);
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(testConfigurableBundle);

        PowerMockito.verifyNew(EventAdminProxy.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ConfigurableBundleNotifierService.class)
                .withArguments(eq(bundleName), eq(mockedHandler), eq(mockedEventAdmin), any());
        verify(mockedContext).registerService(eq(ManagedService.class), eq(mockedConfigService), any());
    }

    @Test
    public void testPersistConfiguration() {
        Dictionary<String, String> props = new Hashtable<>();
        props.put("prop1", "value1");

        Whitebox.setInternalState(testConfigurableBundle, "configServiceRegistration", mockedRegistration);

        testConfigurableBundle.persistConfiguration(props);

        verify(mockedRegistration).setProperties(eq(props));
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testConfigurableBundle, "configServiceRegistration", mockedRegistration);
        Whitebox.setInternalState(testConfigurableBundle, "eventAdmin", mockedEventAdmin);

        testConfigurableBundle.close();

        verify(mockedRegistration).unregister();
        verify(mockedEventAdmin).close();
    }
}