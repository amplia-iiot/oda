package es.amplia.oda.subsystem.sshserver;

import es.amplia.oda.core.commons.utils.ConfigurableBundle;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.core.commons.utils.ServiceListenerBundle;
import es.amplia.oda.subsystem.sshserver.configuration.SshConfigurationUpdateHandler;
import es.amplia.oda.subsystem.sshserver.internal.ConfigurablePasswordAuthenticator;
import es.amplia.oda.subsystem.sshserver.internal.ConfigurablePasswordAuthenticatorImpl;
import es.amplia.oda.subsystem.sshserver.internal.SshCommandShell;

import org.apache.felix.service.command.CommandProcessor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Activator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Activator.class);

    private CommandProcessorProxy commandProcessor;
    private ConfigurationUpdateHandler configHandler;
    private ConfigurableBundle configurableBundle;
    private ServiceListenerBundle<CommandProcessor> commandProcessorListenerBundle;

    private SshCommandShell sshCommandShell;


    @Override
    public void start(BundleContext bundleContext) {
        LOGGER.info("Starting SSH server bundle");

        commandProcessor = new CommandProcessorProxy(bundleContext);
        ConfigurablePasswordAuthenticator passwordAuthenticator = new ConfigurablePasswordAuthenticatorImpl();
        sshCommandShell = new SshCommandShell(commandProcessor, passwordAuthenticator);
        configHandler = new SshConfigurationUpdateHandler(sshCommandShell);
        configurableBundle = new ConfigurableBundleImpl(bundleContext, configHandler);
        commandProcessorListenerBundle =
                new ServiceListenerBundle<>(bundleContext, CommandProcessor.class, this::onServiceChanged);

        LOGGER.info("SSH server bundle started");
    }

    void onServiceChanged() {
        LOGGER.info("Command processor service changed. Re-applying last configuration");
        try {
            configHandler.applyConfiguration();
        } catch (Exception exception) {
            LOGGER.error("Error applying configuration", exception);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) {
        LOGGER.info("Stopping SSH server bundle");

        commandProcessorListenerBundle.close();
        configurableBundle.close();
        commandProcessor.close();
        sshCommandShell.close();

        LOGGER.info("SSH server bundle stopped");
    }
}
