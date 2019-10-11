package es.amplia.oda.subsystem.sshserver.configuration;

import es.amplia.oda.core.commons.utils.ConfigurationUpdateHandler;
import es.amplia.oda.subsystem.sshserver.internal.SshCommandShell;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Optional;

public class SshConfigurationUpdateHandler implements ConfigurationUpdateHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshConfigurationUpdateHandler.class);

    static final String IP_PROPERTY_NAME = "ip";
    static final String PORT_PROPERTY_NAME = "port";
    static final String USERNAME_PROPERTY_NAME = "username";
    static final String PASS_PROPERTY_NAME = "password";

    private final SshCommandShell sshServer;

    private SshConfiguration currentConfiguration;


    public SshConfigurationUpdateHandler(SshCommandShell sshServer) {
        this.sshServer = sshServer;
    }

    @Override
    public void loadConfiguration(Dictionary<String, ?> props) {
        LOGGER.info("Loading new configuration");

        SshConfiguration.SshConfigurationBuilder builder = SshConfiguration.builder();

        Optional.ofNullable((String) props.get(IP_PROPERTY_NAME)).ifPresent(builder::ip);
        Optional.ofNullable((String) props.get(PORT_PROPERTY_NAME)).ifPresent(v -> builder.port(Integer.parseInt(v)));
        Optional.ofNullable((String) props.get(USERNAME_PROPERTY_NAME)).ifPresent(builder::username);
        Optional.ofNullable((String) props.get(PASS_PROPERTY_NAME)).ifPresent(builder::password);

        currentConfiguration = builder.build();

        LOGGER.info("New configuration loaded");
    }

    @Override
    public void applyConfiguration() {
        if (currentConfiguration != null) {
            sshServer.loadConfiguration(currentConfiguration);
            try {
                sshServer.init();
            } catch (IOException e) {
                throw new IllegalArgumentException("Illegal SSH Server configuration");
            }
        }
    }
}
