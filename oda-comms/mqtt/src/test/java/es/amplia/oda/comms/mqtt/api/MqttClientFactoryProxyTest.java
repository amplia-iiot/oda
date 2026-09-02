package es.amplia.oda.comms.mqtt.api;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MqttClientFactoryProxyTest {

    private static final String TEST_SERVER = "test.server.host";
    private static final String TEST_CLIENT_ID = "testClient";

    @Mock
    private BundleContext mockedContext;

    private MqttClientFactoryProxy testProxy;

    private OsgiServiceProxy<MqttClientFactory> mockedOsgiProxy;
    private final List<List<?>> osgiProxyArgs = new ArrayList<>();
    @Captor
    private ArgumentCaptor<Function<MqttClientFactory, MqttClient>> createMqttClientFunctionCaptor;
    @Mock
    private MqttClientFactory mockedFactory;

    @BeforeEach
    @SuppressWarnings("unchecked")
    public void setUp() {
        try (MockedConstruction<OsgiServiceProxy> osgiProxyCons =
                     mockConstruction(OsgiServiceProxy.class,
                             (mock, mctx) -> osgiProxyArgs.add(new ArrayList<>(mctx.arguments())))) {
            testProxy = new MqttClientFactoryProxy(mockedContext);
            mockedOsgiProxy = osgiProxyCons.constructed().get(0);
        }
    }

    @Test
    public void testConstructor() {
        assertEquals(1, osgiProxyArgs.size());
        assertEquals(MqttClientFactory.class, osgiProxyArgs.get(0).get(0));
        assertEquals(mockedContext, osgiProxyArgs.get(0).get(1));
    }

    @Test
    public void testCreateMqttClient() throws MqttException {
        testProxy.createMqttClient(TEST_SERVER, TEST_CLIENT_ID);

        verify(mockedOsgiProxy).callFirst(createMqttClientFunctionCaptor.capture());
        createMqttClientFunctionCaptor.getValue().apply(mockedFactory);
        verify(mockedFactory).createMqttClient(eq(TEST_SERVER), eq(TEST_CLIENT_ID));
    }

    @Test
    public void testCreateMqttClientWrapMqttException() throws MqttException {
        when(mockedFactory.createMqttClient(anyString(), anyString())).thenThrow(new MqttException("", 0));

        testProxy.createMqttClient(TEST_SERVER, TEST_CLIENT_ID);

        verify(mockedOsgiProxy).callFirst(createMqttClientFunctionCaptor.capture());
        assertThrows(MqttClientFactoryProxy.MqttExceptionWrapper.class, () -> createMqttClientFunctionCaptor.getValue().apply(mockedFactory));
    }

    @Test
    public void testCreateMqttClientUnwrapMqttException() throws MqttException {
        when(mockedOsgiProxy.callFirst(any()))
                .thenThrow(new MqttClientFactoryProxy.MqttExceptionWrapper(new MqttException("", 0)));

        assertThrows(MqttException.class, () -> testProxy.createMqttClient(TEST_SERVER, TEST_CLIENT_ID));
    }

    @Test
    public void testClose() {
        testProxy.close();

        verify(mockedOsgiProxy).close();
    }
}
