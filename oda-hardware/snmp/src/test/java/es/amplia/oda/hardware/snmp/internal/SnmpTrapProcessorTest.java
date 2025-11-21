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

    @Mock
    PDU mockedPdu;
    @Mock
    CommandResponderEvent mockedResponderEvent;
    @Mock
    SnmpTranslator mockedSnmpTranslator;

    SnmpTrapProcessor snmpTrapProcessor;

    @Before
    public void start(){
        snmpTrapProcessor = new SnmpTrapProcessor(mockedSnmpTranslator);
        PowerMockito.when(mockedResponderEvent.getPDU()).thenReturn(mockedPdu);
    }

    @Test
    public void processPDUTest(){
        snmpTrapProcessor.processPdu(mockedResponderEvent);
    }
}
