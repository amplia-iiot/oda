package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ModbusMasterManagerTest {

    @Mock
    private ServiceRegistrationManager<ModbusMaster> mockedRegistrationManager;
    @InjectMocks
    private ModbusMasterManager testModbusMasterManager;

    @Mock
    private ModbusMaster mockedModbusMaster;

    @Test
    public void testLoadConfiguration() {
        testModbusMasterManager.loadConfiguration(Collections.singletonList(mockedModbusMaster));

        verify(mockedRegistrationManager).unregister();
        verify(mockedRegistrationManager).register(mockedModbusMaster);
    }

    @Test
    public void testClose() {
        testModbusMasterManager.close();

        verify(mockedRegistrationManager).unregister();
    }
}