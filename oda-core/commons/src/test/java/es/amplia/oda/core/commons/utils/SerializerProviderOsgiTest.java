package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SerializerProviderOsgi.class)
public class SerializerProviderOsgiTest {

    @Mock
    private BundleContext mockedContext;

    private SerializerProviderOsgi testSerializerProvider;

    @Mock
    private SerializerProxy mockedCborSerializer;
    @Mock
    private SerializerProxy mockedJsonSerializer;
    @Mock
    private SerializerProxy mockedMessagePackSerializer;


    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(SerializerProxy.class).withArguments(any(BundleContext.class), eq(ContentType.CBOR))
                .thenReturn(mockedCborSerializer);
        when(mockedCborSerializer.getContentType()).thenReturn(ContentType.CBOR);
        PowerMockito.whenNew(SerializerProxy.class).withArguments(any(BundleContext.class), eq(ContentType.JSON))
                .thenReturn(mockedJsonSerializer);
        when(mockedJsonSerializer.getContentType()).thenReturn(ContentType.JSON);
        PowerMockito.whenNew(SerializerProxy.class).withArguments(any(BundleContext.class), eq(ContentType.MESSAGE_PACK))
                .thenReturn(mockedMessagePackSerializer);
        when(mockedMessagePackSerializer.getContentType()).thenReturn(ContentType.MESSAGE_PACK);

        testSerializerProvider = new SerializerProviderOsgi(mockedContext);
    }

    @Test
    public void testConstructor() throws Exception {
        PowerMockito.verifyNew(SerializerProxy.class).withArguments(eq(mockedContext), eq(ContentType.CBOR));
        PowerMockito.verifyNew(SerializerProxy.class).withArguments(eq(mockedContext), eq(ContentType.JSON));
        PowerMockito.verifyNew(SerializerProxy.class).withArguments(eq(mockedContext), eq(ContentType.MESSAGE_PACK));
    }

    @Test
    public void getSerializer() {
        assertEquals(mockedCborSerializer, testSerializerProvider.getSerializer(ContentType.CBOR));
        assertEquals(mockedJsonSerializer, testSerializerProvider.getSerializer(ContentType.JSON));
        assertEquals(mockedMessagePackSerializer, testSerializerProvider.getSerializer(ContentType.MESSAGE_PACK));
    }

    @Test
    public void testClose() {
        testSerializerProvider.close();

        verify(mockedCborSerializer).close();
        verify(mockedJsonSerializer).close();
        verify(mockedMessagePackSerializer).close();
    }
}