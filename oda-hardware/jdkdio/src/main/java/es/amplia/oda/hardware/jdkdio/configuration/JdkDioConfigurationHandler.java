package es.amplia.oda.hardware.jdkdio.configuration;

import es.amplia.oda.core.commons.gpio.GpioDirection;
import es.amplia.oda.core.commons.gpio.GpioMode;
import es.amplia.oda.core.commons.gpio.GpioTrigger;
import es.amplia.oda.core.commons.utils.Collections;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.hardware.jdkdio.gpio.JdkDioGpioService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Dictionary;
import java.util.Map;
import java.util.Properties;

public class JdkDioConfigurationHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(JdkDioConfigurationHandler.class);

    static final String JDK_DIO_REGISTRY_PROPERTY = "jdk.dio.registry";

    private static final String DEVICE_TYPE_PROPERTY_NAME = "deviceType";
    private static final String NAME_PROPERTY_NAME = "name";
    private static final String DIRECTION_PROPERTY_NAME = "direction";
    private static final String MODE_PROPERTY_NAME = "mode";
    private static final String TRIGGER_PROPERTY_NAME = "trigger";
    private static final String ACTIVE_LOW_PROPERTY_NAME = "activeLow";
    private static final String INITIAL_VALUE_PROPERTY_NAME = "initialValue";

    private static final String GPIO_PIN_DEVICE_TYPE = "gpio.GPIOPin";


    private final JdkDioGpioService jdkDioGpioService;
    private Map<String, ?> gpioPinsConfiguration;


    public JdkDioConfigurationHandler(JdkDioGpioService jdkDioGpioService) {
        this.jdkDioGpioService = jdkDioGpioService;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration");
        gpioPinsConfiguration = Collections.dictionaryToMap(props);
    }

    @Override
    public void loadDefaultConfiguration() throws Exception {
        LOGGER.info("Loading default configuration");
        gpioPinsConfiguration = null;

        String defaultConfigurationFile = System.getProperty(JDK_DIO_REGISTRY_PROPERTY);
        if (defaultConfigurationFile != null) {
            LOGGER.info("Default configuration file: {}", defaultConfigurationFile);
            try {
                Properties properties = new Properties();
                FileInputStream fis = new FileInputStream(defaultConfigurationFile);
                properties.load(fis);
                gpioPinsConfiguration = Collections.propertiesToMap(properties);
            } catch (FileNotFoundException fileNotFoundException) {
                LOGGER.warn("Default configuration file not found");
            }
        }
    }

    @Override
    public void applyConfiguration() {
        jdkDioGpioService.release();

        if (gpioPinsConfiguration != null) {
            for (Map.Entry<String, ?> entry: gpioPinsConfiguration.entrySet()) {
                try {
                    int index = Integer.parseInt((entry.getKey()));
                    String[] tokens = getTokensFromProperty((String) entry.getValue());

                    if (isGpioPinDevice(tokens)) {
                        JdkDioGpioPinBuilder builder = JdkDioGpioPinBuilder.newBuilder();
                        builder.setIndex(index);
                        getValueByToken(NAME_PROPERTY_NAME, tokens).ifPresent(builder::setName);
                        getValueByToken(DIRECTION_PROPERTY_NAME, tokens)
                                .ifPresent(value -> builder.setDirection(GpioDirection.valueOf(value)));
                        getValueByToken(MODE_PROPERTY_NAME, tokens)
                                .ifPresent(value -> builder.setMode(GpioMode.valueOf(value)));
                        getValueByToken(TRIGGER_PROPERTY_NAME, tokens)
                                .ifPresent(value -> builder.setTrigger(GpioTrigger.valueOf(value)));
                        getValueByToken(ACTIVE_LOW_PROPERTY_NAME, tokens)
                                .ifPresent(value -> builder.setActiveLow(Boolean.valueOf(value)));
                        getValueByToken(INITIAL_VALUE_PROPERTY_NAME, tokens)
                                .ifPresent(value -> builder.setInitialValue(Boolean.valueOf(value)));
                        jdkDioGpioService.addConfiguredPin(builder.build());
                    }
                } catch (Exception exception) {
                    LOGGER.warn("Invalid device configuration {}: {}", entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private boolean isGpioPinDevice(String[] tokens) {
        String deviceType = getValueByToken(DEVICE_TYPE_PROPERTY_NAME, tokens).orElse("");
        return deviceType.equals(GPIO_PIN_DEVICE_TYPE);
    }
}
