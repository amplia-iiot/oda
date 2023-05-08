package es.amplia.oda.comms.iec104;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<Object> iec104ServiceRegistration;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting IEC104 Comms bundle");
        iec104ServiceRegistration =
                bundleContext.registerService(Object.class, new Object(), null);
        LOGGER.info("IEC104 Comms bundle started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping MQTT Comms bundle");
        iec104ServiceRegistration.unregister();
        LOGGER.info("MQTT Comms bundle stopped");
    }
}
