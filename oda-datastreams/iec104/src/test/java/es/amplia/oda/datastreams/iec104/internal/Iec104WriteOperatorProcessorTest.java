package es.amplia.oda.datastreams.iec104.internal;

import es.amplia.oda.comms.iec104.master.Iec104ClientModule;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.datastreams.iec104.Iec104ConnectionsFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec104ReadOperatorProcessor.class)
public class Iec104WriteOperatorProcessorTest {

    @Mock
    private Iec104ConnectionsFactory mockedConnectionsFactory;
    @Mock
    private ScadaTableTranslator mockScadaTranslator;
    @Mock
    private Iec104ClientModule mockClient;
    @InjectMocks
    private Iec104WriteOperatorProcessor writeOperatorProcessor;


    @Test
    public void testWrite(){

        // conditions
        when(mockedConnectionsFactory.getConnection("deviceId1")).thenReturn(mockClient);

        // call method
        writeOperatorProcessor.write("deviceId1", "datastreamId1", 1);

        // assertions
        verify(mockedConnectionsFactory, times(1)).getConnection("deviceId1");
    }

}
