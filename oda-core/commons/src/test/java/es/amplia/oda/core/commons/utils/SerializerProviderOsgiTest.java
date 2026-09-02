package es.amplia.oda.core.commons.utils;

import es.amplia.oda.core.commons.entities.ContentType;
import es.amplia.oda.core.commons.osgi.proxies.SerializerProxy;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SerializerProviderOsgiTest {

    @Mock
    private BundleContext mockedContext;

    private SerializerProviderOsgi testSerializerProvider;

    private MockedConstruction<SerializerProxy> serializerConstruction;
    private final List<List<?>> serializerArgs = new ArrayList<>();
    private SerializerProxy mockedCborSerializer;
    private SerializerProxy mockedJsonSerializer;
    private SerializerProxy mockedMessagePackSerializer;


    @Before
    public void setUp() throws Exception {
        serializerConstruction = mockConstruction(SerializerProxy.class,
                (mock, mctx) -> {
                    serializerArgs.add(new ArrayList<>(mctx.arguments()));
                    when(mock.getContentType()).thenReturn((ContentType) mctx.arguments().get(1));
                });

        testSerializerProvider = new SerializerProviderOsgi(mockedContext);

        for (SerializerProxy constructedSerializer : serializerConstruction.constructed()) {
            switch (constructedSerializer.getContentType()) {
                case CBOR:
                    mockedCborSerializer = constructedSerializer;
                    break;
                case JSON:
                    mockedJsonSerializer = constructedSerializer;
                    break;
                case MESSAGE_PACK:
                    mockedMessagePackSerializer = constructedSerializer;
                    break;
            }
        }
    }

    @After
    public void tearDown() {
        serializerConstruction.close();
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals(ContentType.values().length, serializerConstruction.constructed().size());
        for (List<?> constructorArgs : serializerArgs) {
            assertEquals(mockedContext, constructorArgs.get(0));
        }
        assertNotNull(mockedCborSerializer);
        assertNotNull(mockedJsonSerializer);
        assertNotNull(mockedMessagePackSerializer);
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
