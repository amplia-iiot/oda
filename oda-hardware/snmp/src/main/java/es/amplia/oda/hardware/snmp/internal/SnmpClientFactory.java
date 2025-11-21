package es.amplia.oda.hardware.snmp.internal;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.hardware.snmp.configuration.SnmpClientConfig;
import es.amplia.oda.hardware.snmp.configuration.SnmpClientV3Options;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;

@Slf4j
public class SnmpClientFactory {

    public SnmpClient createSnmpClient(SnmpClientConfig conf) {
        Snmp snmpClient;
        CommunityTarget communityTarget;
        UserTarget userTarget;
        try {
            int version = conf.getVersion();
            if (version == 1 || version == 2) {
                communityTarget = communityTarget(conf.getIp(), conf.getPort(), conf.getRetries(), conf.getTimeout(),
                        conf.getOptions().getCommunity());
                snmpClient = buildSnmpClient(conf.getIp(), conf.getPort());
                return new SnmpClientImpl(snmpClient, version, communityTarget, conf.getDeviceId());
            } else if (version == 3) {
                SnmpClientV3Options options = conf.getV3Options();
                OctetString securityName = new OctetString(options.getSecurityName());
                userTarget = userTarget(conf.getIp(), conf.getPort(), conf.getRetries(), conf.getTimeout(), securityName);
                UsmUser user = createV3User(securityName, options.getAuthPassphrase(), options.getPrivPassphrase(),
                        options.getAuthProtocol(), options.getPrivacyProtocol());
                snmpClient = buildSnmpClient(conf.getIp(), conf.getPort());
                snmpClient.getUSM().addUser(securityName, user);
                return new SnmpClientImpl(snmpClient, version, userTarget, options.getContextName(), conf.getDeviceId());
            } else {
                throw new ConfigurationException("Wrong snmp version " + version);
            }
        } catch (Exception e) {
            log.error("Error creating snmp client : ", e);
            return null;
        }
    }

    private Snmp buildSnmpClient(String ip, int port) throws IOException {
        UdpAddress address = new UdpAddress(ip + "/" + port);
        TransportMapping<? extends Address> transport = new DefaultUdpTransportMapping(address);
        return new Snmp(transport);
    }

    private CommunityTarget communityTarget(String ip, int port, int numRetries, int timeout, String community) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community));
        target.setAddress(GenericAddress.parse("udp:" + ip + "/" + port));
        target.setRetries(numRetries);
        target.setTimeout(timeout);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }

    private UserTarget userTarget(String ip, int port, int numRetries, int timeout, OctetString securityName) {
        OctetString localEngineId = new OctetString(MPv3.createLocalEngineID());
        USM usm = new USM(SecurityProtocols.getInstance(), localEngineId, 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        SecurityModels.getInstance().addSecurityModel(new TSM(localEngineId, false));
        UserTarget target = new UserTarget();
        target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
        target.setSecurityName(securityName);
        target.setAddress(GenericAddress.parse("udp:" + ip + "/" + port));
        target.setVersion(SnmpConstants.version3);
        target.setRetries(numRetries);
        target.setTimeout(timeout);
        return target;
    }

    private UsmUser createV3User(OctetString securityName, String authPassphrase, String privPassphrase,
                                 String authProtocol, String privProtocol) {
        OctetString localEngineId = new OctetString(MPv3.createLocalEngineID());
        USM usm = new USM(SecurityProtocols.getInstance(), localEngineId, 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        OID authProtocolOID = getAuthProtocol(authProtocol);
        OID privProtocolOID = getPrivacyProtocol(privProtocol);

        return new UsmUser(securityName, authProtocolOID, new OctetString(authPassphrase), privProtocolOID, new OctetString(privPassphrase));
    }

    private OID getAuthProtocol(String authProtocol) {
        if (authProtocol.equals("MD5")) {
            return AuthMD5.ID;
        } else if (authProtocol.equals("SHA")) {
            return AuthSHA.ID;
        } else {
            throw new ConfigurationException("Authorization protocol " + authProtocol + " not valid");
        }
    }

    private OID getPrivacyProtocol(String privacyProtocol) {
        switch (privacyProtocol) {
            case "DES":
                return PrivDES.ID;
            case "3DES":
                return Priv3DES.ID;
            case "AES128":
                return PrivAES128.ID;
            case "AES192":
                return PrivAES192.ID;
            case "AES256":
                return PrivAES256.ID;
            default:
                throw new ConfigurationException("Privacy protocol " + privacyProtocol + " not valid");
        }
    }
}
