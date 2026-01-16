package es.amplia.oda.comms.http.configuration;

import es.amplia.oda.comms.http.HttpClientFactoryImpl;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Dictionary;
import java.util.Optional;

@Slf4j
public class HttpCommsConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    static final String KEY_STORE_PATH_PROPERTY_NAME = "keyStore.path";
    static final String KEY_STORE_TYPE_PROPERTY_NAME = "keyStore.type";
    static final String KEY_STORE_PASS_PROPERTY_NAME = "keyStore.password";
    static final String TRUST_STORE_PATH_PROPERTY_NAME = "trustStore.path";
    static final String TRUST_STORE_TYPE_PROPERTY_NAME = "trustStore.type";
    static final String TRUST_STORE_PASS_PROPERTY_NAME = "trustStore.password";

    private HttpClientConfiguration currentConfiguration;
    private final HttpClientFactoryImpl clientFactory;

    public HttpCommsConfigurationUpdateHandler(HttpClientFactoryImpl clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        log.info("Loading new configuration");

        // ssl configuration
        Optional<String> keyStorePath = Optional.ofNullable((String) props.get(KEY_STORE_PATH_PROPERTY_NAME));
        Optional<String> keyStoreType = Optional.ofNullable((String) props.get(KEY_STORE_TYPE_PROPERTY_NAME));
        Optional<String> keyStorePassword = Optional.ofNullable((String) props.get(KEY_STORE_PASS_PROPERTY_NAME));
        Optional<String> trustStorePath = Optional.ofNullable((String) props.get(TRUST_STORE_PATH_PROPERTY_NAME));
        Optional<String> trustStoreType = Optional.ofNullable((String) props.get(TRUST_STORE_TYPE_PROPERTY_NAME));
        Optional<String> trustStorePassword = Optional.ofNullable((String) props.get(TRUST_STORE_PASS_PROPERTY_NAME));

        currentConfiguration = HttpClientConfiguration.builder()
                .keyStorePath(keyStorePath.orElse(null))
                .keyStoreType(keyStoreType.orElse(HttpClientConfiguration.DEFAULT_KEY_STORE_TYPE))
                .keyStorePassword(keyStorePassword.orElse(null))
                .trustStorePath(trustStorePath.orElse(null))
                .trustStoreType(trustStoreType.orElse(HttpClientConfiguration.DEFAULT_TRUST_STORE_TYPE))
                .trustStorePassword(trustStorePassword.orElse(null))
                .build();

        log.debug("SSL config : keyStorePath = {}, keyStoreType = {}, keyStorePassword = {}, trustStorePath = {}, " +
                        "trustStoreType = {}, trustStorePassword = {}", currentConfiguration.getKeyStorePath(),
                currentConfiguration.getKeyStoreType(), currentConfiguration.getKeyStorePassword(),
                currentConfiguration.getTrustStorePath(), currentConfiguration.getTrustStoreType(),
                currentConfiguration.getKeyStorePassword());

        log.info("New configuration loaded");
    }

    @Override
    public void applyConfiguration() {
        log.info("Applying last configuration");
        if (currentConfiguration != null) {
            clientFactory.loadConfiguration(currentConfiguration);
        }
        log.info("Last configuration applied");
    }
}
