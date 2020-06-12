package es.amplia.oda.statemanager.inmemory.configuration;

import es.amplia.oda.statemanager.inmemory.InMemoryStateManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.Hashtable;

import static es.amplia.oda.statemanager.inmemory.configuration.StateManagerInMemoryConfigurationHandler.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InMemoryStateManager.class})
public class StateManagerInMemoryConfigurationHandlerTest {

	public static final String DATABASE_PATH_PROPERTY_VALUE = "/123/fake/street";
	public static final String MAX_DATA_PROPERTY_VALUE = "123";
	public static final String TIME_TO_FORGET_OLD_DATA_PROPERTY_VALUE = "600";

	@Mock
	public InMemoryStateManager mockedStateManager;
	@InjectMocks
	public StateManagerInMemoryConfigurationHandler testConfigHandler;

	public StateManagerInMemoryConfiguration config =
			new StateManagerInMemoryConfiguration(
					DATABASE_PATH_PROPERTY_VALUE,
					Integer.parseInt(MAX_DATA_PROPERTY_VALUE),
					Long.parseLong(TIME_TO_FORGET_OLD_DATA_PROPERTY_VALUE)
			);

	@Test
	public void loadConfigurationTest() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(DATABASE_PATH_PROPERTY_NAME, DATABASE_PATH_PROPERTY_VALUE);
		props.put(MAX_DATA_PROPERTY_NAME, MAX_DATA_PROPERTY_VALUE);
		props.put(TIME_TO_FORGET_OLD_DATA_PROPERTY_NAME, TIME_TO_FORGET_OLD_DATA_PROPERTY_VALUE);

		testConfigHandler.loadConfiguration(props);

		assertEquals(DATABASE_PATH_PROPERTY_VALUE, ((StateManagerInMemoryConfiguration)Whitebox.getInternalState(testConfigHandler, "config")).getDatabasePath());
		assertEquals(Integer.parseInt(MAX_DATA_PROPERTY_VALUE), ((StateManagerInMemoryConfiguration)Whitebox.getInternalState(testConfigHandler, "config")).getMaxData());
		assertEquals(Long.parseLong(TIME_TO_FORGET_OLD_DATA_PROPERTY_VALUE), ((StateManagerInMemoryConfiguration)Whitebox.getInternalState(testConfigHandler, "config")).getForgetTime());
	}

	@Test
	public void applyConfigurationTest() {
		Whitebox.setInternalState(testConfigHandler, "config", config);

		testConfigHandler.applyConfiguration();

		verify(this.mockedStateManager).loadConfiguration(eq(config));
	}
}
