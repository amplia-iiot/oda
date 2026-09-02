package es.amplia.oda.subsystem.sshserver.internal;

import es.amplia.oda.subsystem.sshserver.configuration.SshConfiguration;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SshCommandShellTest {

    private static final String TEST_IP = "localhost";
    private static final int TEST_PORT = 1234;
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "test";
    private static final SshConfiguration TEST_CONFIGURATION = SshConfiguration.builder().ip(TEST_IP).port(TEST_PORT)
            .username(TEST_USERNAME).password(TEST_PASSWORD).build();

    private static final String SERVER_FIELD_NAME = "server";
    private static final String IP_FIELD_NAME = "ip";
    private static final String PORT_FIELD_NAME = "port";

    @Mock
    private CommandProcessor mockedCommandProcessor;
    @Mock
    private ConfigurablePasswordAuthenticator mockedPasswordAuthenticator;
    @InjectMocks
    private SshCommandShell testSshCommandShell;

    @Mock
    private SshServer mockedServer;
    @Mock
    private ServerBuilder mockedBuilder;


    @Test
    public void testLoadConfiguration() {
        Whitebox.setInternalState(testSshCommandShell, SERVER_FIELD_NAME, (Object) null);

        testSshCommandShell.loadConfiguration(TEST_CONFIGURATION);

        assertNull(Whitebox.getInternalState(testSshCommandShell, SERVER_FIELD_NAME));
        assertEquals(TEST_IP, Whitebox.getInternalState(testSshCommandShell, IP_FIELD_NAME));
        assertEquals(TEST_PORT, (int) Whitebox.getInternalState(testSshCommandShell, PORT_FIELD_NAME));
        verify(mockedPasswordAuthenticator).loadCredentials(eq(TEST_USERNAME), eq(TEST_PASSWORD));
    }

    @Test
    public void testReloadConfiguration() throws IOException {
        Whitebox.setInternalState(testSshCommandShell, SERVER_FIELD_NAME, mockedServer);

        testSshCommandShell.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedServer).stop();
        assertNull(Whitebox.getInternalState(testSshCommandShell, SERVER_FIELD_NAME));
        assertEquals(TEST_IP, Whitebox.getInternalState(testSshCommandShell, IP_FIELD_NAME));
        assertEquals(TEST_PORT, (int) Whitebox.getInternalState(testSshCommandShell, PORT_FIELD_NAME));
        verify(mockedPasswordAuthenticator).loadCredentials(eq(TEST_USERNAME), eq(TEST_PASSWORD));
    }

    @Test
    public void testReloadConfigurationIOExceptionCaught() throws IOException {
        Whitebox.setInternalState(testSshCommandShell, SERVER_FIELD_NAME, mockedServer);

        doThrow(new IOException()).when(mockedServer).stop();

        testSshCommandShell.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedServer).stop();
        assertNull(Whitebox.getInternalState(testSshCommandShell, SERVER_FIELD_NAME));
        assertEquals(TEST_IP, Whitebox.getInternalState(testSshCommandShell, IP_FIELD_NAME));
        assertEquals(TEST_PORT, (int) Whitebox.getInternalState(testSshCommandShell, PORT_FIELD_NAME));
        verify(mockedPasswordAuthenticator).loadCredentials(eq(TEST_USERNAME), eq(TEST_PASSWORD));
    }

    @Test
    public void testInit() throws Exception {
        Whitebox.setInternalState(testSshCommandShell, IP_FIELD_NAME, TEST_IP);
        Whitebox.setInternalState(testSshCommandShell, PORT_FIELD_NAME, TEST_PORT);

        List<List<?>> shellFactoryArgs = new ArrayList<>();
        List<List<?>> shellCommandFactoryArgs = new ArrayList<>();

        try (MockedStatic<ServerBuilder> serverBuilderStatic = mockStatic(ServerBuilder.class);
             MockedConstruction<ShellFactoryImpl> shellFactoryCons = mockConstruction(ShellFactoryImpl.class,
                     (mock, mctx) -> shellFactoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ShellCommandFactory> shellCommandFactoryCons = mockConstruction(ShellCommandFactory.class,
                     (mock, mctx) -> shellCommandFactoryArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<SimpleGeneratorHostKeyProvider> hostKeyProviderCons = mockConstruction(SimpleGeneratorHostKeyProvider.class);
             MockedConstruction<UserAuthPasswordFactory> userAuthFactoryCons = mockConstruction(UserAuthPasswordFactory.class)) {
            serverBuilderStatic.when(ServerBuilder::builder).thenReturn(mockedBuilder);
            when(mockedBuilder.build()).thenReturn(mockedServer);

            testSshCommandShell.init();

            verify(mockedServer).setHost(eq(TEST_IP));
            verify(mockedServer).setPort(eq(TEST_PORT));
            assertEquals(Collections.singletonList(mockedCommandProcessor), shellFactoryArgs.get(0));
            verify(mockedServer).setShellFactory(eq(shellFactoryCons.constructed().get(0)));
            assertEquals(Collections.singletonList(mockedCommandProcessor), shellCommandFactoryArgs.get(0));
            verify(mockedServer).setCommandFactory(eq(shellCommandFactoryCons.constructed().get(0)));
            assertEquals(1, hostKeyProviderCons.constructed().size());
            verify(mockedServer).setKeyPairProvider(eq(hostKeyProviderCons.constructed().get(0)));
            assertEquals(1, userAuthFactoryCons.constructed().size());
            verify(mockedServer).setUserAuthFactories(eq(Collections.singletonList(userAuthFactoryCons.constructed().get(0))));
            verify(mockedServer).setPasswordAuthenticator(eq(mockedPasswordAuthenticator));
            verify(mockedServer).start();
        }
    }

    @Test
    public void testClose() throws IOException {
        Whitebox.setInternalState(testSshCommandShell, SERVER_FIELD_NAME, mockedServer);

        testSshCommandShell.close();

        verify(mockedServer).stop();
        assertNull(Whitebox.getInternalState(testSshCommandShell, SERVER_FIELD_NAME));
    }

    @Test
    public void testCloseWithNoServer() {
        Whitebox.setInternalState(testSshCommandShell, SERVER_FIELD_NAME, (Object) null);

        testSshCommandShell.close();

        assertNull(Whitebox.getInternalState(testSshCommandShell, SERVER_FIELD_NAME));
    }

    @Test
    public void testCloseIOExceptionCaught() throws IOException {
        Whitebox.setInternalState(testSshCommandShell, SERVER_FIELD_NAME, mockedServer);

        doThrow(new IOException()).when(mockedServer).stop();

        testSshCommandShell.close();

        verify(mockedServer).stop();
        assertNull(Whitebox.getInternalState(testSshCommandShell, SERVER_FIELD_NAME));
    }
}