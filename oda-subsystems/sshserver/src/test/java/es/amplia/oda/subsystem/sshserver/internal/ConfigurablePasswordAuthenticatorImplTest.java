package es.amplia.oda.subsystem.sshserver.internal;

import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;

import static org.junit.Assert.*;

public class ConfigurablePasswordAuthenticatorImplTest {

    private static final String TEST_USER = "test";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String HASH_TEST_PASSWORD =
            "8A5B8B4611DEE46B3DAF3531FABB2A73A93A2BE376EAA240DC115DD5818BD24A533EEEE9A46AAA27C8064516E489E60B75533506E774E1979228428C910AF275";
    private static final String USER_FIELD_NAME = "user";
    private static final String HASH_PASSWORD_FIELD_NAME = "hashPassword";
    private static final String INVALID_VALUE = "INVALID";

    private final ConfigurablePasswordAuthenticatorImpl testConfigPasswordAuthenticator =
            new ConfigurablePasswordAuthenticatorImpl();

    @Test
    public void testLoadCredentials() {
        testConfigPasswordAuthenticator.loadCredentials(TEST_USER, HASH_TEST_PASSWORD);

        assertEquals(TEST_USER, Whitebox.getInternalState(testConfigPasswordAuthenticator, USER_FIELD_NAME));
        assertEquals(HASH_TEST_PASSWORD,
                Whitebox.getInternalState(testConfigPasswordAuthenticator, HASH_PASSWORD_FIELD_NAME));
    }

    @Test
    public void testAuthenticateNotLoadedCredentials() {
        Whitebox.setInternalState(testConfigPasswordAuthenticator, USER_FIELD_NAME, null);
        Whitebox.setInternalState(testConfigPasswordAuthenticator, HASH_PASSWORD_FIELD_NAME, null);

        boolean authentication = testConfigPasswordAuthenticator.authenticate(TEST_USER, TEST_PASSWORD, null);

        assertFalse(authentication);
    }

    @Test
    public void testAuthenticateInvalidUser() {
        Whitebox.setInternalState(testConfigPasswordAuthenticator, USER_FIELD_NAME, TEST_USER);
        Whitebox.setInternalState(testConfigPasswordAuthenticator, HASH_PASSWORD_FIELD_NAME, HASH_TEST_PASSWORD);

        boolean authentication = testConfigPasswordAuthenticator.authenticate(INVALID_VALUE, TEST_PASSWORD, null);

        assertFalse(authentication);
    }

    @Test
    public void testAuthenticateEmptyPasswordConfiguredSuccess() {
        Whitebox.setInternalState(testConfigPasswordAuthenticator, USER_FIELD_NAME, TEST_USER);
        Whitebox.setInternalState(testConfigPasswordAuthenticator, HASH_PASSWORD_FIELD_NAME, null);

        boolean authentication = testConfigPasswordAuthenticator.authenticate(TEST_USER, null, null);

        assertTrue(authentication);
    }

    @Test
    public void testAuthenticateEmptyPasswordConfiguredFailure() {
        Whitebox.setInternalState(testConfigPasswordAuthenticator, USER_FIELD_NAME, TEST_USER);
        Whitebox.setInternalState(testConfigPasswordAuthenticator, HASH_PASSWORD_FIELD_NAME, null);

        boolean authentication = testConfigPasswordAuthenticator.authenticate(TEST_USER, INVALID_VALUE, null);

        assertFalse(authentication);
    }

    @Test
    public void testAuthenticateInvalidPassword() {
        Whitebox.setInternalState(testConfigPasswordAuthenticator, USER_FIELD_NAME, TEST_USER);
        Whitebox.setInternalState(testConfigPasswordAuthenticator, HASH_PASSWORD_FIELD_NAME, HASH_TEST_PASSWORD);

        boolean authentication = testConfigPasswordAuthenticator.authenticate(TEST_USER, INVALID_VALUE, null);

        assertFalse(authentication);
    }

    @Test
    public void testAuthenticateSuccess() {
        Whitebox.setInternalState(testConfigPasswordAuthenticator, USER_FIELD_NAME, TEST_USER);
        Whitebox.setInternalState(testConfigPasswordAuthenticator, HASH_PASSWORD_FIELD_NAME, HASH_TEST_PASSWORD);

        boolean authentication = testConfigPasswordAuthenticator.authenticate(TEST_USER, TEST_PASSWORD, null);

        assertTrue(authentication);
    }
}