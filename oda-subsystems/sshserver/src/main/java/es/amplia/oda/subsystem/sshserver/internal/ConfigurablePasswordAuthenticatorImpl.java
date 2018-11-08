package es.amplia.oda.subsystem.sshserver.internal;

import org.apache.sshd.server.session.ServerSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ConfigurablePasswordAuthenticatorImpl implements ConfigurablePasswordAuthenticator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurablePasswordAuthenticatorImpl.class);

    private static final String ENCRYPTION_ALGORITHM = "SHA-512";
    private static final int HEXADECIMAL = 16;

    private String user;
    private String hashPassword;

    @Override
    public void loadCredentials(String user, String hashPassword) {
        this.user = user;
        this.hashPassword = hashPassword;
    }

    @Override
    public boolean authenticate(String attemptUser, String attemptPassword, ServerSession session) {
        return validateUser(attemptUser) && validatePassword(attemptPassword);


    }

    private boolean validateUser(String attempUser) {
        return user != null && user.equals(attempUser);
    }

    private boolean validatePassword(String attemptPassword) {
        if (hashPassword == null) {
            return attemptPassword == null;
        }

        String generatedPassword = null;
        try {
            MessageDigest md = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
            byte[] bytes = md.digest(attemptPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, HEXADECIMAL).substring(1));
            }
            generatedPassword = sb.toString();
        } catch (NoSuchAlgorithmException exception) {
            LOGGER.error("Error validating password: {}", exception);
        }
        return hashPassword.equalsIgnoreCase(generatedPassword);
    }
}
