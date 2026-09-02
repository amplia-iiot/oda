package es.amplia.oda.statemanager.inmemory.configuration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StateManagerInMemoryConfigurationTest {

	private static final String PATH_FIELD_VALUE = "/country/road/to/home/";
	private static final int MAX_DATA_FIELD_VALUE = 100;
	private static final long FORGET_TIME_FIELD_NAME = 2020;
	private static final long FORGET_PERIOD_FIELD_NAME = 10;
	public static final int TASKS_PROCESSING_THREADS_VALUE = 1;
	public static final int TASKS_PROCESSING_QUEUE_SIZE_VALUE = 10;


	private StateManagerInMemoryConfiguration testConfiguration;

	@BeforeEach
	public void setup() {
		testConfiguration = new StateManagerInMemoryConfiguration(PATH_FIELD_VALUE, MAX_DATA_FIELD_VALUE,
				FORGET_TIME_FIELD_NAME, FORGET_PERIOD_FIELD_NAME, TASKS_PROCESSING_THREADS_VALUE, TASKS_PROCESSING_QUEUE_SIZE_VALUE);
	}

	@Test
	public void testGetPath() {
		assertEquals(PATH_FIELD_VALUE, testConfiguration.getDatabasePath());
	}

	@Test
	public void testGetMaxData() {
		assertEquals(MAX_DATA_FIELD_VALUE, testConfiguration.getMaxData());
	}

	@Test
	public void testGetForgetTime() {
		assertEquals(FORGET_TIME_FIELD_NAME, testConfiguration.getForgetTime());
	}

	@Test
	public void testGetForgetPeriod() {assertEquals(FORGET_PERIOD_FIELD_NAME, testConfiguration.getForgetPeriod());}
}
