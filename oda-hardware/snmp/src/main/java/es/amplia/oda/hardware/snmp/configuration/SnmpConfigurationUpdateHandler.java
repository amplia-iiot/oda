package es.amplia.oda.hardware.snmp.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.core.commons.osgi.proxies.SnmpTranslatorProxy;
import es.amplia.oda.core.commons.snmp.SnmpClient;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.snmp.internal.SnmpClientFactory;
import es.amplia.oda.hardware.snmp.internal.SnmpClientManager;
import es.amplia.oda.hardware.snmp.internal.SnmpTrapListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

@Slf4j
public class SnmpConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final String VERSION_PROPERTY_NAME = "version";
    private static final String IP_PROPERTY_NAME = "ip";
    private static final String PORT_PROPERTY_NAME = "port";
    private static final String TRAP_LISTEN_PORT_PROPERTY_NAME = "trapListenPort";
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
    SnmpTranslatorProxy snmpTrapTranslatorProxy;

    public SnmpConfigurationUpdateHandler(SnmpClientManager snmpClientManager, SnmpClientFactory snmpClientFactory,
                                          SnmpTranslatorProxy snmpTrapTranslator) {
        this.snmpClientManager = snmpClientManager;
        this.snmpClientFactory = snmpClientFactory;
        this.snmpTrapTranslatorProxy = snmpTrapTranslator;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        // disconnect snmp clients
        this.snmpClientManager.close();
        clients.clear();
        // close snmp trap listener
        SnmpTrapListener.closeListener();

        // load new configuration
        Map<String, ?> mappedProperties = Collections.dictionaryToMap(props);

        // get properties for trap listener
        createTrapListener(mappedProperties);

        // get properties for snmp clients
        createSnmpClients(mappedProperties);
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
        String contextName = getValueByToken(CONTEXT_NAME_PROPERTY_NAME, propertyTokens).orElse(null);
        String securityName = getValueByToken(SECURITY_NAME_PROPERTY_NAME, propertyTokens).orElse(null);
        String authPassphrase = getValueByToken(AUTH_PASSPHRASE_PROPERTY_NAME, propertyTokens).orElse(null);
        String privPassphrase = getValueByToken(PRIV_PASSPHRASE_PROPERTY_NAME, propertyTokens).orElse(null);
        String authProtocol = getValueByToken(AUTH_PROTOCOL_PROPERTY_NAME, propertyTokens).orElse(null);
        String privProtocol = getValueByToken(PRIV_PROTOCOL_PROPERTY_NAME, propertyTokens).orElse(null);
        return new SnmpClientV3Options(securityName, authPassphrase, privPassphrase, contextName, authProtocol, privProtocol);
    }

    private void createSnmpClients(Map<String, ?> mappedProperties) {
        // get properties for snmp clients
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
                int version = getValueByToken(VERSION_PROPERTY_NAME, propertyTokens).map(Integer::valueOf)
                        .orElseThrow(throwMissingRequiredPropertyConfigurationException(VERSION_PROPERTY_NAME));
                SnmpClientConfig newClientConfig;
                switch (version) {
                    case 1:
                    case 2:
                        SnmpClientOptions options = parseOptions(propertyTokens);
                        newClientConfig = new SnmpClientConfig(deviceId, ip, port, version, options);
                        break;
                    case 3:
                        SnmpClientV3Options optionsV3 = parseV3Options(propertyTokens);
                        newClientConfig = new SnmpClientConfig(deviceId, ip, port, version, optionsV3);
                        break;
                    default:
                        throw new ConfigurationException("Snmp version not valid");
                }

                // add to list
                log.info("Creating snmp client {} ", newClientConfig);
                SnmpClient snmpClient = snmpClientFactory.createSnmpClient(newClientConfig);
                if (snmpClient != null) {
                    clients.add(snmpClient);
                }

            } catch (Exception e) {
                logInvalidConfigurationWarning(entry, e.getMessage());
            }
        }
    }

    private void createTrapListener(Map<String, ?> mappedProperties) {
        // get listen port
        String listenPortValue = (String) mappedProperties.get(TRAP_LISTEN_PORT_PROPERTY_NAME);
        if (listenPortValue == null) {
            log.warn("Missing required property {}", TRAP_LISTEN_PORT_PROPERTY_NAME);
        } else {
            int listenPort = Integer.parseInt(listenPortValue);
            try {
                SnmpTrapListener.createSnmpListener(listenPort, this.snmpTrapTranslatorProxy);
            } catch (IOException e) {
                log.error("Error creating snmp trap listener : ", e);
            }
            mappedProperties.remove(TRAP_LISTEN_PORT_PROPERTY_NAME);
        }
    }
}
