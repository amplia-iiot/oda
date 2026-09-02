package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.interfaces.SnmpTranslator;
import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.smi.Address;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SnmpTrapProcessorTest {

    @Mock
    PDU mockedPdu;
    @Mock
    CommandResponderEvent mockedResponderEvent;
    @Mock
    SnmpTranslator mockedSnmpTranslator;
    @Mock
    StateManagerProxy mockedStateManager;
    @Mock
    Address mockedAddress;

    Map<String, String> devicesIps = new HashMap<>();
    SnmpTrapProcessor snmpTrapProcessor;

    @BeforeEach
    public void start(){
        snmpTrapProcessor = new SnmpTrapProcessor(mockedSnmpTranslator, mockedStateManager, devicesIps);
        when(mockedResponderEvent.getPDU()).thenReturn(mockedPdu);
        when(mockedResponderEvent.getPeerAddress()).thenReturn(mockedAddress);
        when(mockedAddress.toString()).thenReturn("0.0.0.0");
    }

    @Test
    public void processPDUTest(){
        snmpTrapProcessor.processPdu(mockedResponderEvent);
    }
}
