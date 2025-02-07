package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;

import java.util.Dictionary;
import java.util.Optional;

/**
 * Interface to define how to handle configuration updates
 */
public interface ConfigurationUpdateHandler {

    /**
     * Load the configuration provided as parameter.
     * @param props Configuration to load.
     * @throws RuntimeException Exception during the configuration loading.
     */
    void loadConfiguration(Dictionary<String, ?> props);

    /**
     * Load the default configuration.
     * Interface provide a default implementation throwing an exception to not
     * allow loading the default configuration.
     * @throws RuntimeException Exception during the default configuration loading.
     */
    default void loadDefaultConfiguration(){
        throw new ConfigurationException("Default configuration is not allowed");
    }

    /**
     * Notify the configurable its own configuration file path
     * @param path
     */
    default void notifyConfigurationFilePath(String path) {
        // By default do nothing
    }

    /**
     * Apply configuration.
     * @throws RuntimeException Exception applying configuration.
     */
    void applyConfiguration();

    /**
     * Get the tokens from the given property
     * @param property Property to get the tokens from.
     * @return Property tokens.
     */
    default String[] getTokensFromProperty(String property) {
        return property.split(",");
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
            String[] elements = token.split(":");
            if (tokenName.equals(elements[0].trim())) {
                value = elements[1].trim();
                break;
            }
        }

        return Optional.ofNullable(value);
    }
}
