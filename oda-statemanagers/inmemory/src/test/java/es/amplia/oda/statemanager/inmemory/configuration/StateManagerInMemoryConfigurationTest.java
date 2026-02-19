package es.amplia.oda.statemanager.inmemory.configuration;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest(StateManagerInMemoryConfiguration.class)
public class StateManagerInMemoryConfigurationTest {

	private static final String PATH_FIELD_VALUE = "/country/road/to/home/";
	private static final int MAX_DATA_FIELD_VALUE = 100;
	private static final long FORGET_TIME_FIELD_NAME = 2020;
	private static final long FORGET_PERIOD_FIELD_NAME = 10;
	public static final int TASKS_PROCESSING_THREADS_VALUE = 1;
	public static final int TASKS_PROCESSING_QUEUE_SIZE_VALUE = 10;


	private StateManagerInMemoryConfiguration testConfiguration;

	@Before
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
	public void testGetDbBackupPeriod() {assertEquals(FORGET_PERIOD_FIELD_NAME, testConfiguration.getDbBackupPeriod());}
}
