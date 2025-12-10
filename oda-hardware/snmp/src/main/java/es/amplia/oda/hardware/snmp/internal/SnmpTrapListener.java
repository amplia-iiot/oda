package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.osgi.proxies.SnmpTranslatorProxy;
import es.amplia.oda.core.commons.osgi.proxies.StateManagerProxy;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class SnmpTrapListener {

    private static Snmp trapListener;

    public static void createSnmpListener(int listenPort, Map<String, String> devicesIps, SnmpTranslatorProxy snmpTranslatorProxy,
                                          StateManagerProxy stateManager) throws IOException {
        // it will listen for traps in 0.0.0.0 in port indicated
        String listenIp = "0.0.0.0";
        log.info("Creating snmp listener for ip {} and port {}", listenIp, listenPort);
        UdpAddress address = new UdpAddress(listenIp + "/" + listenPort);
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping(address);
        trapListener = new Snmp(transport);
        // add trap listener
        trapListener.addCommandResponder(new SnmpTrapProcessor(snmpTranslatorProxy, stateManager, devicesIps));
        // start listening
        transport.listen();
    }

    public static void closeListener() {
        if (trapListener != null) {
            try {
                trapListener.close();
            } catch (IOException e) {
                log.error("Error closing snmp listener : ", e);
            }
        }
    }


}
