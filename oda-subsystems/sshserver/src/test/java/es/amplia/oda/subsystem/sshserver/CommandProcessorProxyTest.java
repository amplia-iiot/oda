package es.amplia.oda.subsystem.sshserver;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CommandProcessorProxyTest {

    private CommandProcessorProxy testCommandProcessorProxy;

    @Mock
    private BundleContext mockedContext;
    private OsgiServiceProxy<CommandProcessor> mockedProxy;
    private List<Object> proxyConstructorArgs;
    @Captor
    private ArgumentCaptor<Function<CommandProcessor, CommandSession>> functionCaptor;
    private Function<CommandProcessor, CommandSession> function;
    @Mock
    private CommandProcessor mockedCommandProcessor;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        try (MockedConstruction<OsgiServiceProxy> proxyCons = mockConstruction(OsgiServiceProxy.class,
                (mock, mctx) -> proxyConstructorArgs = new ArrayList<>(mctx.arguments()))) {
            testCommandProcessorProxy = new CommandProcessorProxy(mockedContext);
            mockedProxy = proxyCons.constructed().get(0);
        }
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals(Arrays.asList(CommandProcessor.class, mockedContext), proxyConstructorArgs);
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
