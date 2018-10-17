package es.amplia.oda.subsystem.sshserver.internal;

import org.apache.sshd.server.auth.password.PasswordAuthenticator;

public interface ConfigurablePasswordAuthenticator extends PasswordAuthenticator {
    void loadCredentials(String user, String hashPassword);
}
