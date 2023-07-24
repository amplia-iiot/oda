package es.amplia.oda.service.scadatables;

import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.service.scadatables.configuration.ScadaTablesConfigurationHandler;
import es.amplia.oda.service.scadatables.internal.ScadaTableInfoService;

import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.Collections;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private ServiceRegistration<ScadaTableInfo> scadaTableInfoServiceRegistration;

    private ServiceRegistration<ScadaTableTranslator> scadaTranslatorServiceRegistration;

    private ConfigurableBundle configurableBundle;

    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting SCADA table info");

        ScadaTableInfoService scadaTableService = new ScadaTableInfoService();
        scadaTableInfoServiceRegistration = bundleContext.registerService(ScadaTableInfo.class, scadaTableService, null);
        scadaTranslatorServiceRegistration =
                bundleContext.registerService(ScadaTableTranslator.class, scadaTableService, null);

        ScadaTablesConfigurationHandler configHandler = new ScadaTablesConfigurationHandler(scadaTableService);

        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler,
                Collections.singletonList(scadaTableInfoServiceRegistration));

        LOGGER.info("SCADA table info started");
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping SCADA table info");

        configurableBundle.close();
        scadaTableInfoServiceRegistration.unregister();
        scadaTranslatorServiceRegistration.unregister();

        LOGGER.info("SCADA table info stopped");
    }
}
