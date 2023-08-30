package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.comms.iec104.master.Iec104ClientModule;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.datastreams.iec104.Iec104ConnectionsFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec104DatastreamsFactoryImpl.class)
public class Iec104DatastreamsFactoryImplTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";


    private Iec104DatastreamsFactoryImpl testFactory;
    @Mock
    private Iec104ConnectionsFactory mockedConnectionsFactory;
    @Mock
    private Iec104ClientModule mockedClientModule;
    @Mock
    private ScadaTableTranslator mockedScadaTranslator;
    @Mock
    private Iec104ReadOperatorProcessor mockedReadOperatorProcessor;
    @Mock
    private Iec104WriteOperatorProcessor mockedWriteOperatorProcessor;
    @Mock
    private Iec104DatastreamsGetter mockedDatastreamsGetter;
    @Mock
    private Iec104DatastreamsSetter mockedDatastreamsSetter;

    @Before
    public void setUp() throws Exception {
        PowerMockito.whenNew(Iec104ReadOperatorProcessor.class).withAnyArguments()
                .thenReturn(mockedReadOperatorProcessor);
        PowerMockito.whenNew(Iec104WriteOperatorProcessor.class).withAnyArguments()
                .thenReturn(mockedWriteOperatorProcessor);
        PowerMockito.when(mockedConnectionsFactory.getConnection(anyString())).thenReturn(mockedClientModule);

        testFactory = new Iec104DatastreamsFactoryImpl(mockedScadaTranslator, mockedConnectionsFactory);
    }

    @Test
    public void testConstructor() throws Exception {
        PowerMockito.verifyNew(Iec104ReadOperatorProcessor.class)
                .withArguments(eq(mockedScadaTranslator), eq(mockedConnectionsFactory));
        PowerMockito.verifyNew(Iec104WriteOperatorProcessor.class)
                .withArguments(eq(mockedScadaTranslator), eq(mockedConnectionsFactory));
    }

    @Test
    public void testCreateIec104DatastreamsGetter() throws Exception {
        PowerMockito.whenNew(Iec104DatastreamsGetter.class).withAnyArguments().thenReturn(mockedDatastreamsGetter);

        Iec104DatastreamsGetter result =
                testFactory.createIec104DatastreamsGetter(TEST_DATASTREAM_ID);

        assertEquals(mockedDatastreamsGetter, result);
        PowerMockito.verifyNew(Iec104DatastreamsGetter.class).withArguments(eq(TEST_DATASTREAM_ID), any(List.class), eq(mockedReadOperatorProcessor));
        PowerMockito.verifyNew(Iec104ReadOperatorProcessor.class)
                .withArguments(eq(mockedScadaTranslator), eq(mockedConnectionsFactory));
    }

    @Test
    public void testCreateIec104DatastreamsSetter() throws Exception {
        PowerMockito.whenNew(Iec104DatastreamsSetter.class).withAnyArguments().thenReturn(mockedDatastreamsSetter);

        Iec104DatastreamsSetter result =
                testFactory.createIec104DatastreamsSetter(TEST_DATASTREAM_ID);

        assertEquals(mockedDatastreamsSetter, result);
        PowerMockito.verifyNew(Iec104DatastreamsSetter.class).withArguments(eq(TEST_DATASTREAM_ID), any(List.class), eq(mockedWriteOperatorProcessor));
        PowerMockito.verifyNew(Iec104WriteOperatorProcessor.class)
                .withArguments(eq(mockedScadaTranslator), eq(mockedConnectionsFactory));
    }

    @Test
    public void testUpdateGetterPolling(){
        testFactory.updateGetterPolling(100, 200);
        verify(mockedReadOperatorProcessor, times(1)).updateGetterPolling(100, 200);

    }
}