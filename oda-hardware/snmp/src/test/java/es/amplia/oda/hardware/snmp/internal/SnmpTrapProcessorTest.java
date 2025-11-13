package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.powermock.api.mockito.PowerMockito;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;

@RunWith(MockitoJUnitRunner.class)
public class SnmpTrapProcessorTest {

    private final String DEVICE_ID_TEST = "deviceIdTest";

    @Mock
    PDU mockedPdu;
    @Mock
    CommandResponderEvent mockedResponderEvent;
    @Mock
    SnmpTranslator mockedSnmpTranslator;

    SnmpTrapProcessor snmpTrapProcessor;

    @Before
    public void start(){
        snmpTrapProcessor = new SnmpTrapProcessor(DEVICE_ID_TEST, mockedSnmpTranslator);
        PowerMockito.when(mockedResponderEvent.getPDU()).thenReturn(mockedPdu);
    }

    @Test
    public void processPDUTest(){
        snmpTrapProcessor.processPdu(mockedResponderEvent);
    }
}
