package es.amplia.oda.service.scadatables;

import es.amplia.oda.core.commons.interfaces.ScadaTableInfo;
import es.amplia.oda.core.commons.interfaces.ScadaTableTranslator;
import es.amplia.oda.core.commons.utils.ConfigurableBundleImpl;
import es.amplia.oda.service.scadatables.configuration.ScadaTablesConfigurationHandler;
import es.amplia.oda.service.scadatables.internal.ScadaTableInfoService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ActivatorTest {
    private final Activator testActivator = new Activator();

    @Mock
    private ConfigurableBundleImpl mockedConfigurableBundleImpl;
    @Mock
    private ServiceRegistration<ScadaTableInfo> mockedServiceRegistrationScadaTableInfo;
    @Mock
    private ServiceRegistration<ScadaTableTranslator> mockedServiceRegistrationScadaTableTranslator;

    @Mock
    private BundleContext mockedContext;

    @Test
    public void testStart() throws Exception {
        List<List<?>> handlerArgs = new ArrayList<>();
        List<List<?>> configurableBundleArgs = new ArrayList<>();
        try (MockedConstruction<ScadaTableInfoService> infoServiceConstruction =
                     mockConstruction(ScadaTableInfoService.class);
             MockedConstruction<ScadaTablesConfigurationHandler> handlerConstruction =
                     mockConstruction(ScadaTablesConfigurationHandler.class,
                             (mock, mctx) -> handlerArgs.add(new ArrayList<>(mctx.arguments())));
             MockedConstruction<ConfigurableBundleImpl> configurableBundleConstruction =
                     mockConstruction(ConfigurableBundleImpl.class,
                             (mock, mctx) -> configurableBundleArgs.add(new ArrayList<>(mctx.arguments())))) {

            testActivator.start(mockedContext);

            assertEquals(1, infoServiceConstruction.constructed().size());
            assertEquals(1, handlerConstruction.constructed().size());
            assertEquals(infoServiceConstruction.constructed().get(0), handlerArgs.get(0).get(0));
            assertEquals(1, configurableBundleConstruction.constructed().size());
            assertEquals(mockedContext, configurableBundleArgs.get(0).get(0));
            assertEquals(handlerConstruction.constructed().get(0), configurableBundleArgs.get(0).get(1));
        }
    }

    @Test
    public void testStop() {
        Whitebox.setInternalState(testActivator, "scadaTableInfoServiceRegistration", mockedServiceRegistrationScadaTableInfo);
        Whitebox.setInternalState(testActivator, "scadaTranslatorServiceRegistration", mockedServiceRegistrationScadaTableTranslator);
        Whitebox.setInternalState(testActivator, "configurableBundle", mockedConfigurableBundleImpl);

        testActivator.stop(mockedContext);

        verify(mockedServiceRegistrationScadaTableInfo).unregister();
        verify(mockedServiceRegistrationScadaTableTranslator).unregister();
        verify(mockedConfigurableBundleImpl).close();
    }
}
