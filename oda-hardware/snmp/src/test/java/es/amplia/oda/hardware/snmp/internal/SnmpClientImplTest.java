package es.amplia.oda.hardware.snmp.internal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.event.ResponseEvent;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SnmpClientImplTest {

    private final String DEVICE_ID_TEST = "deviceIdTest";
    private final int VERSION_1_TEST = 1;
    private final int VERSION_3_TEST = 3;
    private final String DATATYPE_TEST = "String";
    private final String NEW_VALUE_TEST = "newValue";
    private final String OID_TEST = "1.3.2.4.5.0";
    private final String CONTEXT_NAME_TEST = "contextNameTest";


    @Mock
    PDU mockedPdu;
    @Mock
    ResponseEvent mockedResponseEvent;
    @Mock
    Snmp mockedSnmp;

    SnmpClientImpl snmpClientImpl;

    @BeforeEach
    public void start() throws IOException {
        when(mockedSnmp.send(Mockito.any(), Mockito.any())).thenReturn(mockedResponseEvent);
        when(mockedResponseEvent.getResponse()).thenReturn(mockedPdu);
        when(mockedPdu.getErrorStatus()).thenReturn(0);
    }

    @Test
    public void getDeviceIdTest() {
        snmpClientImpl = new SnmpClientImpl(mockedSnmp, VERSION_1_TEST, null, DEVICE_ID_TEST);

        String actualDeviceId = snmpClientImpl.getDeviceId();

        Assertions.assertEquals(DEVICE_ID_TEST, actualDeviceId);
    }

    @Test
    public void disconnectTest() throws IOException {
        snmpClientImpl = new SnmpClientImpl(mockedSnmp, VERSION_1_TEST, null, DEVICE_ID_TEST);

        snmpClientImpl.disconnect();

        verify(mockedSnmp).close();
    }

    @Test
    public void getValueV1Test() {

        snmpClientImpl = new SnmpClientImpl(mockedSnmp, VERSION_1_TEST, null, DEVICE_ID_TEST);

        Object actualResponse = snmpClientImpl.getValue(OID_TEST);

        Assertions.assertNull(actualResponse);
    }

    @Test
    public void getValueV3Test() {

        snmpClientImpl = new SnmpClientImpl(mockedSnmp, VERSION_3_TEST, null, CONTEXT_NAME_TEST, DEVICE_ID_TEST);

        Object actualResponse = snmpClientImpl.getValue(OID_TEST);

        Assertions.assertNull(actualResponse);
    }

    @Test
    public void setValueV1Test() {

        snmpClientImpl = new SnmpClientImpl(mockedSnmp, VERSION_1_TEST, null, DEVICE_ID_TEST);

        snmpClientImpl.setValue(OID_TEST, DATATYPE_TEST, NEW_VALUE_TEST);
    }

    @Test
    public void setValueV3Test() {

        snmpClientImpl = new SnmpClientImpl(mockedSnmp, VERSION_3_TEST, null, CONTEXT_NAME_TEST, DEVICE_ID_TEST);

        snmpClientImpl.setValue(OID_TEST, DATATYPE_TEST, NEW_VALUE_TEST);
    }
}
