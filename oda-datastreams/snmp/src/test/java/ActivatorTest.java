import es.amplia.oda.core.commons.osgi.proxies.SnmpTranslatorProxy;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.datastreams.snmp.Activator;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import es.amplia.oda.datastreams.snmp.configuration.SnmpDatastreamsConfigurationHandler;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
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
    private SnmpDatastreamsConfigurationHandler mockedConfigHandler;
    @Mock
    private SnmpClientsFinder mockedSnmpClientsFinder;
    @Mock
    private SnmpDatastreamsManager mockedSnmpDatastreamsManager;
    @Mock
    private SnmpTranslatorProxy mockedSnmpTranslatorProxy;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;

    @Test
    public void testStart() throws Exception {
        PowerMockito.whenNew(SnmpTranslatorProxy.class).withAnyArguments().thenReturn(mockedSnmpTranslatorProxy);
        PowerMockito.whenNew(SnmpClientsFinder.class).withAnyArguments().thenReturn(mockedSnmpClientsFinder);
        PowerMockito.whenNew(SnmpDatastreamsConfigurationHandler.class).withAnyArguments().thenReturn(mockedConfigHandler);
        PowerMockito.whenNew(ConfigurableBundleImpl.class).withAnyArguments().thenReturn(mockedConfigurableBundle);
        PowerMockito.whenNew(SnmpDatastreamsManager.class).withAnyArguments().thenReturn(mockedSnmpDatastreamsManager);

        testActivator.start(mockedContext);

        PowerMockito.verifyNew(SnmpClientsFinder.class).withArguments(eq(mockedContext));
        PowerMockito.verifyNew(ConfigurableBundleImpl.class).withArguments(eq(mockedContext), eq(mockedConfigHandler));
        PowerMockito.verifyNew(SnmpDatastreamsManager.class).withArguments(eq(mockedSnmpClientsFinder),
                Mockito.any(), Mockito.any(), Mockito.any());

    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundle);
        Whitebox.setInternalState(testActivator, "snmpClientsFinder", mockedSnmpClientsFinder);
        Whitebox.setInternalState(testActivator, "snmpDatastreamsManager", mockedSnmpDatastreamsManager);


        testActivator.stop(mockedContext);

        verify(mockedConfigurableBundle).close();
        verify(mockedSnmpClientsFinder).close();
        verify(mockedSnmpDatastreamsManager).close();
    }
}