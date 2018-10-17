package es.amplia.oda.subsystem.sshserver.configuration;

import es.amplia.oda.subsystem.sshserver.internal.SshCommandShell;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static es.amplia.oda.subsystem.sshserver.configuration.SshConfigurationUpdateHandler.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SshConfigurationUpdateHandlerTest {

    private static final String TEST_IP = "localhost";
    private static final int TEST_PORT = 1234;
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "test";
    private static final SshConfiguration TEST_CONFIGURATION = SshConfiguration.builder().ip(TEST_IP).port(TEST_PORT)
            .username(TEST_USERNAME).password(TEST_PASSWORD).build();

    private static final String CURRENT_CONFIGURATION_FIELD_NAME = "currentConfiguration";

    @Mock
    private SshCommandShell mockedCommandShell;
    @InjectMocks
    private SshConfigurationUpdateHandler testHandler;

    @Test
    public void testLoadConfiguration() {
        Dictionary<String, String> conf = new Hashtable<>();
        conf.put(IP_PROPERTY_NAME, TEST_IP);
        conf.put(PORT_PROPERTY_NAME, String.valueOf(TEST_PORT));
        conf.put(USERNAME_PROPERTY_NAME, String.valueOf(TEST_USERNAME));
        conf.put(PASSWORD_PROPERTY_NAME, String.valueOf(TEST_PASSWORD));

        testHandler.loadConfiguration(conf);

        assertEquals(TEST_CONFIGURATION, Whitebox.getInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLoadConfigurationWithMissingFields() {
        Dictionary<String, String> conf = new Hashtable<>();
        conf.put(IP_PROPERTY_NAME, TEST_IP);
        conf.put(PORT_PROPERTY_NAME, String.valueOf(TEST_PORT));

        testHandler.loadConfiguration(conf);

        fail("Null pointer exception must be thrown");
    }

    @Test
    public void testApplyConfiguration() throws IOException {
        Whitebox.setInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME, TEST_CONFIGURATION);

        testHandler.applyConfiguration();

        verify(mockedCommandShell).loadConfiguration(eq(TEST_CONFIGURATION));
        verify(mockedCommandShell).init();
    }

    @Test
    public void testApplyConfigurationNoCurrentConfiguration() throws IOException {
        Whitebox.setInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME, null);

        testHandler.applyConfiguration();

        verify(mockedCommandShell, never()).loadConfiguration(eq(TEST_CONFIGURATION));
        verify(mockedCommandShell, never()).init();
    }
}