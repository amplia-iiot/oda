package es.amplia.oda.service.cborserializer;

import es.amplia.oda.core.commons.interfaces.Serializer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ActivatorTest {

    private final Activator activator = new Activator();

    @Mock
    private BundleContext mockedContext;
    @Mock
    private ServiceRegistration<?> mockedRegistration;

    @Test
    public void testStart() {
        activator.start(mockedContext);
        
        verify(mockedContext).registerService(eq(Serializer.class), any(), any());
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(activator, "registration", mockedRegistration);

        activator.stop(mockedContext);
        
        verify(mockedRegistration).unregister();
    }
}