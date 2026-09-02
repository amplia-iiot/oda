package es.amplia.oda.subsystem.sshserver.configuration;

import es.amplia.oda.subsystem.sshserver.internal.SshCommandShell;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import static es.amplia.oda.subsystem.sshserver.configuration.SshConfigurationUpdateHandler.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SshConfigurationUpdateHandlerTest {

    private static final String TEST_IP = "localhost";
    private static final int TEST_PORT = 1234;
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASS = "test";
    private static final SshConfiguration TEST_CONFIGURATION = SshConfiguration.builder().ip(TEST_IP).port(TEST_PORT)
            .username(TEST_USERNAME).password(TEST_PASS).build();

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
        conf.put(USERNAME_PROPERTY_NAME, TEST_USERNAME);
        conf.put(PASS_PROPERTY_NAME, TEST_PASS);

        testHandler.loadConfiguration(conf);

        assertEquals(TEST_CONFIGURATION, Whitebox.getInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME));
    }

    @Test
    public void testLoadConfigurationWithMissingFields() {
        Dictionary<String, String> conf = new Hashtable<>();
        conf.put(IP_PROPERTY_NAME, TEST_IP);
        conf.put(PORT_PROPERTY_NAME, String.valueOf(TEST_PORT));

        assertThrows(IllegalArgumentException.class, () -> testHandler.loadConfiguration(conf));
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
        Whitebox.setInternalState(testHandler, CURRENT_CONFIGURATION_FIELD_NAME, (Object) null);

        testHandler.applyConfiguration();

        verify(mockedCommandShell, never()).loadConfiguration(eq(TEST_CONFIGURATION));
        verify(mockedCommandShell, never()).init();
    }
}