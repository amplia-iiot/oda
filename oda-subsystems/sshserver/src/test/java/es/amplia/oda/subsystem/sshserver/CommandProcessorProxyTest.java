package es.amplia.oda.subsystem.sshserver;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


@RunWith(PowerMockRunner.class)
@PrepareForTest(CommandProcessorProxy.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class CommandProcessorProxyTest {

    private CommandProcessorProxy testCommandProcessorProxy;

    @Mock
    private BundleContext mockedContext;
    @Mock
    private OsgiServiceProxy<CommandProcessor> mockedProxy;
    @Captor
    private ArgumentCaptor<Function<CommandProcessor, CommandSession>> functionCaptor;
    private Function<CommandProcessor, CommandSession> function;
    @Mock
    private CommandProcessor mockedCommandProcessor;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(OsgiServiceProxy.class).withAnyArguments().thenReturn(mockedProxy);

        testCommandProcessorProxy = new CommandProcessorProxy(mockedContext);
    }

    @Test
    public void testConstructor() throws Exception {
        PowerMockito.verifyNew(OsgiServiceProxy.class).withArguments(eq(CommandProcessor.class), eq(mockedContext));
    }

    @Test
    public void testCreateSession() {
        InputStream mockedInputStream = mock(InputStream.class);
        OutputStream mockedOutputStream = mock(OutputStream.class);
        OutputStream mockedErrorStream = mock(OutputStream.class);

        testCommandProcessorProxy.createSession(mockedInputStream, mockedOutputStream, mockedErrorStream);

        verify(mockedProxy).callFirst(functionCaptor.capture());
        function = functionCaptor.getValue();
        function.apply(mockedCommandProcessor);
        verify(mockedCommandProcessor)
                .createSession(eq(mockedInputStream), eq(mockedOutputStream), eq(mockedErrorStream));
    }

    @Test
    public void testCreateSessionFromParentSession() {
        CommandSession mockedSession = mock(CommandSession.class);

        testCommandProcessorProxy.createSession(mockedSession);

        verify(mockedProxy).callFirst(functionCaptor.capture());
        function = functionCaptor.getValue();
        function.apply(mockedCommandProcessor);
        verify(mockedCommandProcessor).createSession(eq(mockedSession));
    }

    @Test
    public void testClose() {
        testCommandProcessorProxy.close();

        verify(mockedProxy).close();
    }
}