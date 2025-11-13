package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SnmpClientManagerTest {

    private final List<SnmpClient> SNMP_CLIENTS_LIST_TEST = new ArrayList<>();


    @Mock
    SnmpClient mockedSnmpClient;
    @Mock
    ServiceRegistrationManager<SnmpClient> mockedServiceRegistration;
    @InjectMocks
    SnmpClientManager snmpClientManager;

    @Before
    public void start()
    {
        this.SNMP_CLIENTS_LIST_TEST.add(mockedSnmpClient);
    }

    @Test
    public void loadConfigurationTest(){

        snmpClientManager.loadConfiguration(SNMP_CLIENTS_LIST_TEST);

        verify(mockedServiceRegistration).unregister();
        verify(mockedServiceRegistration).register(mockedSnmpClient);
    }

    @Test
    public void closeTest(){

        snmpClientManager.loadConfiguration(SNMP_CLIENTS_LIST_TEST);
        snmpClientManager.close();

        verify(mockedServiceRegistration, times(2)).unregister();
        verify(mockedSnmpClient).disconnect();
    }
}
