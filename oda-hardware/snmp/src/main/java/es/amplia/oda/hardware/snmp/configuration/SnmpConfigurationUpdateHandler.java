package es.amplia.oda.hardware.snmp.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.snmp.internal.SnmpClientFactory;
import es.amplia.oda.hardware.snmp.internal.SnmpClientManager;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class SnmpConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final String VERSION_PROPERTY_NAME = "version";
    private static final String IP_PROPERTY_NAME = "ip";
    private static final String PORT_PROPERTY_NAME = "port";
    private static final String LISTEN_PORT_PROPERTY_NAME = "listenPort";
    private static final String COMMUNITY_PROPERTY_NAME = "community";
    private static final String CONTEXT_NAME_PROPERTY_NAME = "contextName";
    private static final String SECURITY_NAME_PROPERTY_NAME = "securityName";
    private static final String AUTH_PASSPHRASE_PROPERTY_NAME = "authPassphrase";
    private static final String PRIV_PASSPHRASE_PROPERTY_NAME = "privPassphrase";
    private static final String AUTH_PROTOCOL_PROPERTY_NAME = "authProtocol";
    private static final String PRIV_PROTOCOL_PROPERTY_NAME = "privProtocol";

    List<SnmpClient> clients = new ArrayList<>();

    SnmpClientManager snmpClientManager;
    SnmpClientFactory snmpClientFactory;

    public SnmpConfigurationUpdateHandler(SnmpClientManager snmpClientManager, SnmpClientFactory snmpClientFactory) {
        this.snmpClientManager = snmpClientManager;
        this.snmpClientFactory = snmpClientFactory;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        this.snmpClientManager.disconnectClients();
        clients.clear();

        Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);
        for (Map.Entry<String, ?> entry : mappedProperties.entrySet()) {
            try {
                // key is deviceId
                String[] keyProperties = getTokensFromProperty(entry.getKey());
                String deviceId = keyProperties[0].trim();

                // properties are version, ip, port, community
                String[] propertyTokens = getTokensFromProperty((String) entry.getValue());

                String ip = getValueByToken(IP_PROPERTY_NAME, propertyTokens)
                        .orElseThrow(throwMissingRequiredPropertyConfigurationException(IP_PROPERTY_NAME));
                int port = getValueByToken(PORT_PROPERTY_NAME, propertyTokens).map(Integer::valueOf)
                        .orElseThrow(throwMissingRequiredPropertyConfigurationException(PORT_PROPERTY_NAME));
                int listenPort = getValueByToken(LISTEN_PORT_PROPERTY_NAME, propertyTokens).map(Integer::valueOf)
                        .orElseThrow(throwMissingRequiredPropertyConfigurationException(LISTEN_PORT_PROPERTY_NAME));
                int version = getValueByToken(VERSION_PROPERTY_NAME, propertyTokens).map(Integer::valueOf)
                        .orElseThrow(throwMissingRequiredPropertyConfigurationException(VERSION_PROPERTY_NAME));
                SnmpClientConfig newClientConfig;
                switch (version) {
                    case 1:
                    case 2:
                        SnmpClientOptions options = parseOptions(propertyTokens);
                        newClientConfig = new SnmpClientConfig(deviceId, ip, port, listenPort, version, options);
                        break;
                    case 3:
                        SnmpClientV3Options optionsV3 = parseV3Options(propertyTokens);
                        newClientConfig = new SnmpClientConfig(deviceId, ip, port, listenPort, version, optionsV3);
                        break;
                    default:
                        throw new ConfigurationException("Snmp version not valid");
                }

                // add to list
                log.info("Adding snmp client {} ", newClientConfig);
                clients.add(snmpClientFactory.createSnmpClient(newClientConfig));

            } catch (Exception e) {
                logInvalidConfigurationWarning(entry, e.getMessage());
            }
        }
    }

    @Override
    public void applyConfiguration() {
        snmpClientManager.loadConfiguration(clients);
        log.info("New SNMP Hardware configuration loaded");
    }

    private Supplier<RuntimeException> throwMissingRequiredPropertyConfigurationException(String propertyName) {
        return () -> new ConfigurationException("Missing required property \"" + propertyName + "\"");
    }

    private void logInvalidConfigurationWarning(Map.Entry<String, ?> entry, String message) {
        log.warn("Invalid configuration entry  \"{}\": {}", entry, message);
    }

    private SnmpClientOptions parseOptions(String[] propertyTokens) {
        String community = getValueByToken(COMMUNITY_PROPERTY_NAME, propertyTokens).orElse(null);
        return new SnmpClientOptions(community);
    }

    private SnmpClientV3Options parseV3Options(String[] propertyTokens) {
        String contextName = getValueByToken(CONTEXT_NAME_PROPERTY_NAME, propertyTokens)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(CONTEXT_NAME_PROPERTY_NAME));
        String securityName = getValueByToken(SECURITY_NAME_PROPERTY_NAME, propertyTokens)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(SECURITY_NAME_PROPERTY_NAME));
        String authPassphrase = getValueByToken(AUTH_PASSPHRASE_PROPERTY_NAME, propertyTokens)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(AUTH_PASSPHRASE_PROPERTY_NAME));
        String privPassphrase = getValueByToken(PRIV_PASSPHRASE_PROPERTY_NAME, propertyTokens)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(PRIV_PASSPHRASE_PROPERTY_NAME));
        String authProtocol = getValueByToken(AUTH_PROTOCOL_PROPERTY_NAME, propertyTokens)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(AUTH_PROTOCOL_PROPERTY_NAME));
        String privProtocol = getValueByToken(PRIV_PROTOCOL_PROPERTY_NAME, propertyTokens)
                .orElseThrow(throwMissingRequiredPropertyConfigurationException(PRIV_PROTOCOL_PROPERTY_NAME));
        return new SnmpClientV3Options(securityName, authPassphrase, privPassphrase, contextName, authProtocol, privProtocol);
    }
}
