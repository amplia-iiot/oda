package internal;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.interfaces.DatastreamsSetter;
import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import es.amplia.oda.core.commons.snmp.SnmpEntry;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;
import es.amplia.oda.datastreams.snmp.SnmpClientsFinder;
import es.amplia.oda.datastreams.snmp.internal.SnmpDatastreamsManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SnmpDatastreamsManagerTest {

    private static final String TEST_DEVICE_ID_VALUE = "testDevice";
    private static final String TEST_OID_VALUE = "1.3.6.1.2.1.1.7.0";
    private static final String TEST_DATATYPE_VALUE = "String";
    private static final String TEST_DATASTREAM_VALUE = "testDatastreamId1";
    private static final String TEST_FEED_VALUE = "feed";

    List<SnmpEntry> snmpEntries = new ArrayList<>();

    @Mock
    SnmpClientsFinder mockedClientsFinder;
    @Mock
    ServiceRegistrationManager<DatastreamsGetter> mockedDatastreamsGetterRegistrationManager;
    @Mock
    ServiceRegistrationManager<DatastreamsSetter> mockedDatastreamsSetterRegistrationManager;
    @Mock
    ServiceRegistrationManager<SnmpTranslator> mockedSnmpTranslatorRegistrationManager;

    SnmpDatastreamsManager snmpDatastreamsManager;

    @Before
    public void start(){
        snmpDatastreamsManager = new SnmpDatastreamsManager(mockedClientsFinder, mockedDatastreamsGetterRegistrationManager,
                mockedDatastreamsSetterRegistrationManager, mockedSnmpTranslatorRegistrationManager);

        SnmpEntry snmpEntry = new SnmpEntry(TEST_OID_VALUE, TEST_DATATYPE_VALUE, TEST_DATASTREAM_VALUE,
                TEST_DEVICE_ID_VALUE, TEST_FEED_VALUE);
        snmpEntries.add(snmpEntry);
    }

    @Test
    public void loadConfigurationTest(){
        snmpDatastreamsManager.loadConfiguration(snmpEntries);

        verify(mockedDatastreamsGetterRegistrationManager).unregister();
        verify(mockedDatastreamsSetterRegistrationManager).unregister();
        verify(mockedSnmpTranslatorRegistrationManager).unregister();
        verify(mockedDatastreamsGetterRegistrationManager).register(Mockito.any());
        verify(mockedSnmpTranslatorRegistrationManager).register(Mockito.any());
    }

    @Test
    public void closeTest(){
        snmpDatastreamsManager.close();

        verify(mockedDatastreamsGetterRegistrationManager).unregister();
        verify(mockedDatastreamsSetterRegistrationManager).unregister();
        verify(mockedSnmpTranslatorRegistrationManager).unregister();
    }


}
