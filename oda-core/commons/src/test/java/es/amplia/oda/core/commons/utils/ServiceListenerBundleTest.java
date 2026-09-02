package es.amplia.oda.core.commons.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceListener;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ServiceListenerBundleTest {

    private interface TestService {}

    @Mock
    private BundleContext mockedContext;
    @Mock
    private Runnable mockedAction;

    private ServiceListenerBundle<TestService> testServiceListener;

    @Mock
    private ServiceListener mockedListener;

    @BeforeEach
    public void setUp() {
        testServiceListener = new ServiceListenerBundle<>(mockedContext, TestService.class, mockedAction);
    }

    @Test
    public void testConstructor() throws Exception {
        assertNotNull(testServiceListener);

        verify(mockedContext)
                .addServiceListener(any(ServiceListener.class),
                        eq(String.format("(%s=%s)", Constants.OBJECTCLASS, TestService.class.getName())));
    }

    @Test
    public void testClose() {
        Whitebox.setInternalState(testServiceListener, "serviceListener", mockedListener);

        testServiceListener.close();

        verify(mockedContext).removeServiceListener(eq(mockedListener));
    }
}