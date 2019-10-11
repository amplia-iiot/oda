package es.amplia.oda.subsystem.sshserver.internal;

import es.amplia.oda.subsystem.sshserver.configuration.SshConfiguration;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.sshd.server.ServerBuilder;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.UserAuthPasswordFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ SshCommandShell.class, ServerBuilder.class })
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
    @Mock
    private ShellFactoryImpl mockedShellFactory;
    @Mock
    private ShellCommandFactory mockedShellCommandFactory;
    @Mock
    private SimpleGeneratorHostKeyProvider mockedHostKeyProvider;
    @Mock
    private UserAuthPasswordFactory mockedUserAuthFactory;


    @Test
    public void testLoadConfiguration() {
        Whitebox.setInternalState(testSshCommandShell, SERVER_FIELD_NAME, null);

        testSshCommandShell.loadConfiguration(TEST_CONFIGURATION);

        assertNull(Whitebox.getInternalState(testSshCommandShell, SERVER_FIELD_NAME));
        assertEquals(TEST_IP, Whitebox.getInternalState(testSshCommandShell, IP_FIELD_NAME));
        assertEquals(TEST_PORT, Whitebox.getInternalState(testSshCommandShell, PORT_FIELD_NAME));
        verify(mockedPasswordAuthenticator).loadCredentials(eq(TEST_USERNAME), eq(TEST_PASSWORD));
    }

    @Test
    public void testReloadConfiguration() throws IOException {
        Whitebox.setInternalState(testSshCommandShell, SERVER_FIELD_NAME, mockedServer);

        testSshCommandShell.loadConfiguration(TEST_CONFIGURATION);

        verify(mockedServer).stop();
        assertNull(Whitebox.getInternalState(testSshCommandShell, SERVER_FIELD_NAME));
        assertEquals(TEST_IP, Whitebox.getInternalState(testSshCommandShell, IP_FIELD_NAME));
        assertEquals(TEST_PORT, Whitebox.getInternalState(testSshCommandShell, PORT_FIELD_NAME));
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
        assertEquals(TEST_PORT, Whitebox.getInternalState(testSshCommandShell, PORT_FIELD_NAME));
        verify(mockedPasswordAuthenticator).loadCredentials(eq(TEST_USERNAME), eq(TEST_PASSWORD));
    }

    @Test
    public void testInit() throws Exception {
        Whitebox.setInternalState(testSshCommandShell, IP_FIELD_NAME, TEST_IP);
        Whitebox.setInternalState(testSshCommandShell, PORT_FIELD_NAME, TEST_PORT);

        PowerMockito.mockStatic(ServerBuilder.class);
        PowerMockito.when(ServerBuilder.builder()).thenReturn(mockedBuilder);
        when(mockedBuilder.build()).thenReturn(mockedServer);
        PowerMockito.whenNew(ShellFactoryImpl.class).withAnyArguments().thenReturn(mockedShellFactory);
        PowerMockito.whenNew(ShellCommandFactory.class).withAnyArguments().thenReturn(mockedShellCommandFactory);
        PowerMockito.whenNew(SimpleGeneratorHostKeyProvider.class).withAnyArguments().thenReturn(mockedHostKeyProvider);
        PowerMockito.whenNew(UserAuthPasswordFactory.class).withAnyArguments().thenReturn(mockedUserAuthFactory);

        testSshCommandShell.init();

        mockedServer.setHost(eq(TEST_IP));
        mockedServer.setPort(eq(TEST_PORT));
        PowerMockito.verifyNew(ShellFactoryImpl.class).withArguments(eq(mockedCommandProcessor));
        verify(mockedServer).setShellFactory(eq(mockedShellFactory));
        PowerMockito.verifyNew(ShellCommandFactory.class).withArguments(eq(mockedCommandProcessor));
        verify(mockedServer).setCommandFactory(eq(mockedShellCommandFactory));
        PowerMockito.verifyNew(SimpleGeneratorHostKeyProvider.class).withNoArguments();
        verify(mockedServer).setKeyPairProvider(eq(mockedHostKeyProvider));
        PowerMockito.verifyNew(UserAuthPasswordFactory.class).withNoArguments();
        verify(mockedServer).setUserAuthFactories(eq(Collections.singletonList(mockedUserAuthFactory)));
        verify(mockedServer).setPasswordAuthenticator(eq(mockedPasswordAuthenticator));
        verify(mockedServer).start();
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
        Whitebox.setInternalState(testSshCommandShell, SERVER_FIELD_NAME, null);

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