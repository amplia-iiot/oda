package es.amplia.oda.hardware.atserver;

import es.amplia.oda.hardware.atmanager.ATManagerImpl;
import es.amplia.oda.hardware.atmanager.ATParserImpl;
import es.amplia.oda.hardware.atmanager.api.ATManager;
import es.amplia.oda.core.commons.utils.ConfigurableBundleNotifierService;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;

import lombok.Value;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.EventAdmin;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.TooManyListenersException;

public class ATServerActivator implements BundleActivator, ConfigurationUpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(ATServerActivator.class);
    Properties props;
    private SerialPort commPort = null;
    private ServiceRegistration<?> atManagerRegistration = null;
    private ServiceRegistration<?> configServiceRegistration = null;
    private BundleContext bundleContext = null;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("AT Server starting");
        bundleContext = context;

        ServiceTracker<EventAdmin, EventAdmin> eventAdminServiceTracker = null;
        eventAdminServiceTracker = new ServiceTracker<>(bundleContext, EventAdmin.class, null);
        eventAdminServiceTracker.open();
        EventAdmin eventAdmin = eventAdminServiceTracker.getService();

        String bundleName = bundleContext.getBundle().getSymbolicName();
        ConfigurableBundleNotifierService configService = new ConfigurableBundleNotifierService(bundleName, this, eventAdmin);
        Dictionary<String, String> props = new Hashtable<>();
        props.put("service.pid", bundleName);
        configServiceRegistration = bundleContext.registerService(ManagedService.class, configService, props);
        eventAdminServiceTracker.close();
        logger.info("AT Server started");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("AT Server stopping");
        halt();
        if (configServiceRegistration != null) {
            configServiceRegistration.unregister();
            configServiceRegistration = null;
        }
        logger.info("AT Server stopped");
    }

    @SuppressWarnings("unchecked")
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

    private void halt() {
        if (atManagerRegistration != null) {
            atManagerRegistration.unregister();
            atManagerRegistration = null;
        }
        if (commPort != null) {
            commPort.close();
            commPort = null;
        }
    }

    private Properties getProps(Dictionary<String, ?> properties) throws ConfigurationException {
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
        return new Properties(appName, millisecondsToGetPort, portName, baudRate, dataBits, stopBits, parityAsInt);
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> properties) throws Exception {
        halt();
        logger.debug("AT Server updated with {} properties", properties.size());
        props = getProps(properties);
    }

    @Override
    public void applyConfiguration() throws Exception {
        try {
            commPort = (SerialPort) CommPortIdentifier.getPortIdentifier(props.getPortName()).open(props.getAppName(), props.getMillisecondsToGetPort());
            if (commPort == null) {
                logger.error("Cannot open {} as AT comm port", props.getPortName());
                throw new ConfigurationException("port-name", "Cannot open as AT comm port");
            }
            commPort.setSerialPortParams(props.getBaudRate(), props.getDataBits(), props.getStopBits(), props.getParity());
            logger.info("Opened {} as AT comm port {} {}{}{}", props.getPortName(), props.getBaudRate(), props.getDataBits(), props.getParity(), props.getStopBits());

            ATManager atManager;
            atManager = new ATManagerImpl(new ATParserImpl(), commPort.getOutputStream());

            commPort.addEventListener((evt -> {
                try {
                    if (evt.getEventType() != SerialPortEvent.DATA_AVAILABLE) return;
                    InputStream input = commPort.getInputStream();
                    BufferedReader buffer = new BufferedReader(new InputStreamReader(input));
                    while (buffer.ready()) {
                        String line = buffer.readLine();
                        logger.info("Received in comm port: \"{}\"", line);
                        atManager.process(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            commPort.notifyOnDataAvailable(true);

            atManagerRegistration = bundleContext.registerService(ATManager.class.getName(), atManager, null);
        } catch (NoSuchPortException e) {
            logger.error("", e);
            halt();
            throw new ConfigurationException("port-name", "No such port");
        } catch (PortInUseException e) {
            logger.error("", e);
            halt();
            throw new ConfigurationException("port-name", "Port already in use");
        } catch (UnsupportedCommOperationException e) {
            logger.error("", e);
            halt();
            throw new ConfigurationException("port-name", "The combination of baud-rate, data-bits, stop-bits and parity is not supported");
        } catch (IOException e) {
            logger.error("", e);
            halt();
            throw new ConfigurationException("port-name", "Error getting output stream");
        } catch (TooManyListenersException e) {
            logger.error("", e);
            halt();
            throw new ConfigurationException("port-name", "Too many listeners");
        }

    }

    @Value
    private static class Properties {
        String appName;
        int millisecondsToGetPort;
        String portName;
        int baudRate;
        int dataBits;
        int stopBits;
        int parity;
    }
}
