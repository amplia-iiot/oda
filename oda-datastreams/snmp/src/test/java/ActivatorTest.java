import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManagerOsgi;
import es.amplia.oda.datastreams.snmp.Activator;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import es.amplia.oda.datastreams.snmp.configuration.SnmpDatastreamsConfigurationHandler;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsManager;
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
    private SnmpClientsFinder mockedSnmpClientsFinder;
    @Mock
    private SnmpDatastreamsManager mockedSnmpDatastreamsManager;
    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundle;

    @Test
    public void testStart() {
        List<List<?>> finderArgs = new ArrayList<>();
        List<List<?>> managerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();

        try (MockedConstruction<SnmpClientsFinder> finderCons = mockConstruction(SnmpClientsFinder.class,
                (mock, mctx) -> finderArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ServiceRegistrationManagerOsgi> registrationManagerCons =
                     mockConstruction(ServiceRegistrationManagerOsgi.class);
             MockedConstruction<SnmpDatastreamsManager> managerCons = mockConstruction(SnmpDatastreamsManager.class,
                     (mock, mctx) -> managerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SnmpDatastreamsConfigurationHandler> configHandlerCons =
                     mockConstruction(SnmpDatastreamsConfigurationHandler.class);
             MockedConstruction<ConfigurableBundleImpl> configurableBundleCons =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, finderCons.constructed().size());
            assertEquals(mockedContext, finderArgs.get(0).get(0));
            assertEquals(1, configurableBundleCons.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(configHandlerCons.constructed().get(0), configurableBundleArgs.get(0).get(1));
            assertEquals(1, managerCons.constructed().size());
            assertEquals(finderCons.constructed().get(0), managerArgs.get(0).get(0));
            assertEquals(4, managerArgs.get(0).size());
        }
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
