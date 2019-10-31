package es.amplia.oda.datastreams.deviceinfofx30.datastreams;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class DatastreamGetterTemplateTest {

	private DatastreamGetterTemplate testTemplate;

	private String datastreamId = "deviceState";
	private String expectedState = "OK. When it's broken, I'll warn you";

	@Before
	public void setUp() {
		testTemplate = new DatastreamGetterTemplate(datastreamId, ()-> expectedState);
	}

	@Test
	public void testGetDatastreamIdSatisfied() {
		String datastreamId = testTemplate.getDatastreamIdSatisfied();

		assertEquals(this.datastreamId, datastreamId);
	}

	@Test
	public void testGetDevicesIdManged() {
		List<String> devices = testTemplate.getDevicesIdManaged();

		assertEquals(Collections.singletonList(""), devices);
	}

	@Test
	public void testGet() throws ExecutionException, InterruptedException {
		String state = (String) testTemplate.get("dumbDevice").get().getValue();

		assertEquals(expectedState, state);
	}
}
