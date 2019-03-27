package es.amplia.oda.comms.mqtt.api;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.function.Function;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MqttClientFactoryProxy.class)
public class MqttClientFactoryProxyTest {

    private static final String TEST_SERVER = "test.server.host";
    private static final String TEST_CLIENT_ID = "testClient";

    @Mock
    private BundleContext mockedContext;

    private MqttClientFactoryProxy testProxy;

    @Mock
    private OsgiServiceProxy<MqttClientFactory> mockedOsgiProxy;
    @Captor
    private ArgumentCaptor<Function<MqttClientFactory, MqttClient>> createMqttClientFunctionCaptor;
    @Mock
    private MqttClientFactory mockedFactory;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(OsgiServiceProxy.class).withAnyArguments().thenReturn(mockedOsgiProxy);

        testProxy = new MqttClientFactoryProxy(mockedContext);
    }

    @Test
    public void testConstructor() throws Exception {
        PowerMockito.verifyNew(OsgiServiceProxy.class).withArguments(eq(MqttClientFactory.class), eq(mockedContext));
    }

    @Test
    public void testCreateMqttClient() throws MqttException {
        testProxy.createMqttClient(TEST_SERVER, TEST_CLIENT_ID);

        verify(mockedOsgiProxy).callFirst(createMqttClientFunctionCaptor.capture());
        createMqttClientFunctionCaptor.getValue().apply(mockedFactory);
        verify(mockedFactory).createMqttClient(eq(TEST_SERVER), eq(TEST_CLIENT_ID));
    }

    @Test(expected = MqttClientFactoryProxy.MqttExceptionWrapper.class)
    public void testCreateMqttClientWrapMqttException() throws MqttException {
        when(mockedFactory.createMqttClient(anyString(), anyString())).thenThrow(new MqttException(""));

        testProxy.createMqttClient(TEST_SERVER, TEST_CLIENT_ID);

        verify(mockedOsgiProxy).callFirst(createMqttClientFunctionCaptor.capture());
        createMqttClientFunctionCaptor.getValue().apply(mockedFactory);
    }

    @Test(expected = MqttException.class)
    public void testCreateMqttClientUnwrapMqttException() throws MqttException {
        when(mockedOsgiProxy.callFirst(any()))
                .thenThrow(new MqttClientFactoryProxy.MqttExceptionWrapper(new MqttException("")));

        testProxy.createMqttClient(TEST_SERVER, TEST_CLIENT_ID);
    }

    @Test
    public void testClose() {
        testProxy.close();

        verify(mockedOsgiProxy).close();
    }
}