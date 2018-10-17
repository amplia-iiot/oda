package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import java.util.Dictionary;
import java.util.Optional;

/**
 * Interface to define how to handle configuration updates
 */
public interface ConfigurationUpdateHandler {
    /**
     * Default split property tokens string.
     */
    String SPLIT_TOKENS_STRING = ",";

    /**
     * Default split property value string.
     */
    String SPLIT_VALUE_STRING = ":";

    /**
     * Default configuration not allowed default exception message
     */
    String DEFAULT_CONFIGURATION_NOT_ALLOWED = "Default configuration is not allowed";

    /**
     * Load the configuration provided as parameter.
     * @param props Configuration to load.
     * @throws Exception Exception during the configuration loading.
     */
    void loadConfiguration(Dictionary<String, ?> props) throws Exception;

    /**
     * Load the default configuration.
     * Interface provide a default implementation throwing an exception to not
     * allow loading the default configuration.
     * @throws Exception Exception during the default configuration loading.
     */
    default void loadDefaultConfiguration() throws Exception {
        throw new ConfigurationException(DEFAULT_CONFIGURATION_NOT_ALLOWED);
    }

    /**
     * Apply configuration.
     * @throws Exception Exception applying configuration.
     */
    void applyConfiguration() throws Exception;

    /**
     * Get the tokens from the given property
     * @param property Property to get the tokens from.
     * @return Property tokens.
     */
    default String[] getTokensFromProperty(String property) {
        return property.split(SPLIT_TOKENS_STRING);
    }

    /**
     * Get the value for the given token name.
     * @param tokenName Token name to get the value.
     * @param tokens Tokens to check.
     * @return Value of the token with the given name if exists.
     */
    default Optional<String> getValueByToken(String tokenName, String[] tokens) {
        String value = null;

        for (String token: tokens) {
            String[] elements = token.split(SPLIT_VALUE_STRING);
            if (tokenName.equals(elements[0].trim())) {
                value = elements[1].trim();
                break;
            }
        }

        return Optional.ofNullable(value);
    }
}
