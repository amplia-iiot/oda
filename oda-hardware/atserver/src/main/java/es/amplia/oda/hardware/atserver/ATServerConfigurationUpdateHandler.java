package es.amplia.oda.hardware.atserver;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;

import java.util.Dictionary;

class ATServerConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ATServerConfigurationUpdateHandler.class);

    private final ATServer atServer;

    private ATServerConfiguration currentConfiguration;

    ATServerConfigurationUpdateHandler(ATServer atServer) {
        this.atServer = atServer;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> properties) throws Exception {
        LOGGER.debug("AT Server updated with {} properties", properties.size());

        String appName = get(properties, "app-name", String.class);
        int millisecondsToGetPort = get(properties, "ms-get-port", Integer.class);
        String portName = get(properties, "port-name", String.class);
        int baudRate = get(properties, "baud-rate", Integer.class);
        int dataBits = get(properties, "data-bits", Integer.class);
        int stopBits = get(properties, "stop-bits", Integer.class);
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
                throw new ConfigurationException("parity", "Parity value error. Only 'N', 'O', 'E', 'M' and 'S' are supported");
        }

        currentConfiguration = new ATServerConfiguration(appName, millisecondsToGetPort, portName, baudRate, dataBits,
                stopBits, parityAsInt);
    }

    private <T> T get(Dictionary<String, ?> properties, String key, Class<T> clazz) throws ConfigurationException {
        Object v = properties.get(key);
        if (v == null) {
            throw new ConfigurationException(key, "Property not found in config");
        }
        if (!(v instanceof String)) {
            throw new ConfigurationException(key, "Value in config is not a String");
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
                throw new ConfigurationException(key, "Value is not an Integer");
            }
        }
        if (clazz == Character.class) {
            if (value.length() == 1) {
                Character c = value.charAt(0);
                return (T) c;
            }
            throw new ConfigurationException(key, "Property has more than one char");
        }
        throw new ConfigurationException(key, "Not implemented exception. Type " + clazz.getName() + " not parseable");
    }

    @Override
    public void applyConfiguration() throws ConfigurationException {
        atServer.loadConfiguration(currentConfiguration);
    }
}
