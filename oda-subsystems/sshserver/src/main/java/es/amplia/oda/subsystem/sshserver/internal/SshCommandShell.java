package es.amplia.oda.subsystem.sshserver.internal;

import es.amplia.oda.subsystem.sshserver.configuration.SshConfiguration;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;

public class SshCommandShell implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SshCommandShell.class);

    private final CommandProcessor processor;
    private final ConfigurablePasswordAuthenticator passwordAuthenticator;

    private String ip;
    private int port;
    private SshServer server;

    public SshCommandShell(CommandProcessor processor, ConfigurablePasswordAuthenticator passwordAuthenticator) {
        this.processor = processor;
        this.passwordAuthenticator = passwordAuthenticator;
    }

    public void loadConfiguration(SshConfiguration currentConfiguration) {
        stop();
        this.ip = currentConfiguration.getIp();
        this.port = currentConfiguration.getPort();
        passwordAuthenticator.loadCredentials(currentConfiguration.getUsername(), currentConfiguration.getPassword());
    }

    public void init() throws IOException {
        server = ServerBuilder.builder().build();
        server.setPort(port);
        server.setHost(ip);
        server.setShellFactory(new ShellFactoryImpl(processor));
        server.setCommandFactory(new ShellCommandFactory(processor));
        server.setKeyPairProvider(new SimpleGeneratorHostKeyProvider());
        server.setUserAuthFactories(Collections.singletonList(
                new UserAuthPasswordFactory()
        ));
        server.setPasswordAuthenticator(passwordAuthenticator);
        server.start();
        LOGGER.info("SSH command shell listening on {}:{}", ip, port);
    }

    private void stop() {
        if (server != null) {
            try {
                server.stop();
            } catch (IOException exception) {
                LOGGER.error("Error stopping SSH command shell", exception);
            }
            server = null;
        }
    }

    @Override
    public void close() {
        stop();
    }
}
