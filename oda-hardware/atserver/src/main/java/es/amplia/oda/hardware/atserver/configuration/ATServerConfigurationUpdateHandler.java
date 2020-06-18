package es.amplia.oda.hardware.atserver.configuration;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import es.amplia.oda.hardware.atserver.ATServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;

import java.util.Dictionary;

public class ATServerConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ATServerConfigurationUpdateHandler.class);

    private final ATServer atServer;

    private ATServerConfiguration currentConfiguration;

    public ATServerConfigurationUpdateHandler(ATServer atServer) {
        this.atServer = atServer;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> properties) {
        LOGGER.info("Loading new configuration for ATServer bundle");
        LOGGER.debug("AT Server updated with {} properties", properties.size());

        String appName = get(properties, "appName", String.class);
        int timeToGetPort = get(properties, "timeToGetPort", Integer.class);
        String portName = get(properties, "portName", String.class);
        int baudRate = get(properties, "baudRate", Integer.class);
        int dataBits = get(properties, "dataBits", Integer.class);
        int stopBits = get(properties, "stopBits", Integer.class);
        Character parity = get(properties, "parity", Character.class);
        int parityAsInt;
        switch (parity) {
            case 'N':
                parityAsInt = SerialPort.PARITY_NONE;
                break;
            case 'O':
                parityAsInt = SerialPort.PARITY_ODD;
                break;
            case 'E':
                parityAsInt = SerialPort.PARITY_EVEN;
                break;
            case 'M':
                parityAsInt = SerialPort.PARITY_MARK;
                break;
            case 'S':
                parityAsInt = SerialPort.PARITY_SPACE;
                break;
            default:
                throw new IllegalArgumentException("Parity value error. Only 'N', 'O', 'E', 'M' and 'S' are supported");
        }
        long timeBetweenCommands = get(properties, "timeBetweenCommands", Long.class, 0L);

        currentConfiguration = new ATServerConfiguration(appName, timeToGetPort, portName, baudRate, dataBits,
                stopBits, parityAsInt, timeBetweenCommands);
        LOGGER.info("ATServer bundle new configuration loaded");
    }

    @SuppressWarnings({"unchecked", "WrapperTypeMayBePrimitive"})
    private <T> T get(Dictionary<String, ?> properties, String key, Class<T> clazz, T defaultValue) {
        Object v = properties.get(key);
        if (v == null) {
            if (defaultValue == null) {
                throw new IllegalAccessError(key + " not found in config");
            }
            return defaultValue;
        }
        if (!(v instanceof String)) {
            throw new IllegalArgumentException(key + " in config is not a String");
        }
        String value = (String) v;
        if (clazz == String.class) {
            return (T) value;
        }
        if (clazz == Integer.class) {
            try {
                Integer i = Integer.parseInt(value);
                return (T) i;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(key + " in config is not a Integer");
            }
        }
        if (clazz == Long.class) {
            try {
                Long l = Long.parseLong(value);
                return (T) l;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(key + " in config is not a Long");
            }
        }
        if (clazz == Character.class) {
            if (value.length() == 1) {
                Character c = value.charAt(0);
                return (T) c;
            }
            throw new IllegalArgumentException("Property has more than one char");
        }
        throw new IllegalArgumentException("Not implemented exception. Type " + clazz.getName() + " not parseable");
    }

    private <T> T get(Dictionary<String, ?> properties, String key, Class<T> clazz) {
        return get(properties, key, clazz, null);
    }

    @Override
    public void applyConfiguration() {
        atServer.loadConfiguration(currentConfiguration);
    }
}
