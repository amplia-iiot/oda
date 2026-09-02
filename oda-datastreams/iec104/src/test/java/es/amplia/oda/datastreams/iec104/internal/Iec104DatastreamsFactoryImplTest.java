package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.comms.iec104.master.Iec104ClientModule;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.datastreams.iec104.Iec104ConnectionsFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class Iec104DatastreamsFactoryImplTest {

    private static final String TEST_DATASTREAM_ID = "testDatastream";


    private Iec104DatastreamsFactoryImpl testFactory;
    @Mock
    private Iec104ConnectionsFactory mockedConnectionsFactory;
    @Mock
    private Iec104ClientModule mockedClientModule;
    @Mock
    private ScadaTableTranslator mockedScadaTranslator;

    private MockedConstruction<Iec104ReadOperatorProcessor> readOperatorProcessorCons;
    private MockedConstruction<Iec104WriteOperatorProcessor> writeOperatorProcessorCons;
    private final List<List<?>> readOperatorProcessorArgs = new ArrayList<>();
    private final List<List<?>> writeOperatorProcessorArgs = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        readOperatorProcessorCons = mockConstruction(Iec104ReadOperatorProcessor.class,
                (mock, mctx) -> readOperatorProcessorArgs.add(new ArrayList<>(mctx.arguments())));
        writeOperatorProcessorCons = mockConstruction(Iec104WriteOperatorProcessor.class,
                (mock, mctx) -> writeOperatorProcessorArgs.add(new ArrayList<>(mctx.arguments())));
        when(mockedConnectionsFactory.getConnection(anyString())).thenReturn(mockedClientModule);

        testFactory = new Iec104DatastreamsFactoryImpl(mockedScadaTranslator, mockedConnectionsFactory);
    }

    @After
    public void tearDown() {
        writeOperatorProcessorCons.close();
        readOperatorProcessorCons.close();
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals(1, readOperatorProcessorCons.constructed().size());
        assertEquals(mockedScadaTranslator, readOperatorProcessorArgs.get(0).get(0));
        assertEquals(mockedConnectionsFactory, readOperatorProcessorArgs.get(0).get(1));
        assertEquals(1, writeOperatorProcessorCons.constructed().size());
        assertEquals(mockedScadaTranslator, writeOperatorProcessorArgs.get(0).get(0));
        assertEquals(mockedConnectionsFactory, writeOperatorProcessorArgs.get(0).get(1));
    }

    @Test
    public void testCreateIec104DatastreamsGetter() throws Exception {
        List<List<?>> getterArgs = new ArrayList<>();
        try (MockedConstruction<Iec104DatastreamsGetter> getterCons = mockConstruction(Iec104DatastreamsGetter.class,
                (mock, mctx) -> getterArgs.add(new ArrayList<>(mctx.arguments())))) {

            Iec104DatastreamsGetter result =
                    testFactory.createIec104DatastreamsGetter(TEST_DATASTREAM_ID);

            assertEquals(1, getterCons.constructed().size());
            assertEquals(getterCons.constructed().get(0), result);
            assertEquals(TEST_DATASTREAM_ID, getterArgs.get(0).get(0));
            assertEquals(readOperatorProcessorCons.constructed().get(0), getterArgs.get(0).get(2));
        }
    }

    @Test
    public void testCreateIec104DatastreamsSetter() throws Exception {
        List<List<?>> setterArgs = new ArrayList<>();
        try (MockedConstruction<Iec104DatastreamsSetter> setterCons = mockConstruction(Iec104DatastreamsSetter.class,
                (mock, mctx) -> setterArgs.add(new ArrayList<>(mctx.arguments())))) {

            Iec104DatastreamsSetter result =
                    testFactory.createIec104DatastreamsSetter(TEST_DATASTREAM_ID);

            assertEquals(1, setterCons.constructed().size());
            assertEquals(setterCons.constructed().get(0), result);
            assertEquals(TEST_DATASTREAM_ID, setterArgs.get(0).get(0));
            assertEquals(writeOperatorProcessorCons.constructed().get(0), setterArgs.get(0).get(2));
        }
    }
}
