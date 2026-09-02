package es.amplia.oda.hardware.modbus.internal;

import es.amplia.oda.core.commons.modbus.ModbusMaster;
import es.amplia.oda.core.commons.utils.ServiceRegistrationManager;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
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