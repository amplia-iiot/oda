package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.hardware.snmp.configuration.SnmpClientConfig;
import es.amplia.oda.hardware.snmp.configuration.SnmpClientOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.snmp4j.CommandResponderEvent;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.mp.StatusInformation;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Talks to a real SNMP agent over UDP on localhost instead of mocking snmp4j.
 * Regression for 4.16.0, where the client bound to the device address and never listened,
 * so GET and SET failed against any device.
 */
public class SnmpClientAgentTest {

    private static final String DEVICE_ID = "testDevice";
    private static final String OID = "1.3.6.1.2.1.1.5.0";
    private static final String AGENT_VALUE = "oda-test-agent";

    private Snmp agent;
    private int agentPort;
    private final AtomicReference<VariableBinding> lastSet = new AtomicReference<>();
    private SnmpClient client;

    @Before
    public void startAgent() throws IOException {
        DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping(new UdpAddress("127.0.0.1/0"));
        agent = new Snmp(transport);
        agent.addCommandResponder(this::respond);
        agent.listen();
        agentPort = transport.getListenAddress().getPort();
    }

    @After
    public void stop() throws IOException {
        if (client != null) {
            client.disconnect();
        }
        agent.close();
    }

    @Test
    public void getReturnsAgentValue() {
        client = createClient();

        assertNotNull("factory must build a client for a device on the same host", client);
        assertEquals(AGENT_VALUE, client.getValue(OID));
    }

    @Test
    public void setReachesAgent() {
        client = createClient();

        client.setValue(OID, "STRING", "new-name");

        VariableBinding received = lastSet.get();
        assertNotNull("agent did not receive the SET", received);
        assertEquals(OID, received.getOid().toDottedString());
        assertEquals("new-name", received.getVariable().toString());
    }

    private SnmpClient createClient() {
        SnmpClientConfig config = new SnmpClientConfig(DEVICE_ID, "127.0.0.1", agentPort, 2,
                new SnmpClientOptions("public"));
        return new SnmpClientFactory().createSnmpClient(config);
    }

    private void respond(CommandResponderEvent event) {
        PDU request = event.getPDU();
        if (request == null || request.getType() == PDU.RESPONSE) {
            return;
        }
        PDU response = new PDU(request);
        response.setType(PDU.RESPONSE);
        response.setErrorStatus(PDU.noError);
        if (request.getType() == PDU.SET) {
            lastSet.set(request.get(0));
        } else {
            for (VariableBinding vb : response.getVariableBindings()) {
                vb.setVariable(new OctetString(AGENT_VALUE));
            }
        }
        try {
            event.getMessageDispatcher().returnResponsePdu(event.getMessageProcessingModel(),
                    event.getSecurityModel(), event.getSecurityName(), event.getSecurityLevel(), response,
                    event.getMaxSizeResponsePDU(), event.getStateReference(), new StatusInformation());
            event.setProcessed(true);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
