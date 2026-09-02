package es.amplia.oda.datastreams.deviceinfo.datastreams;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.TestCase.assertEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DatastreamGetterTemplateTest {

	private DatastreamGetterTemplate testTemplate;

	private String datastreamId = "deviceState";
	private String expectedState = "OK. When it's broken, I'll warn you";

	@BeforeEach
	public void setUp() {
		testTemplate = new DatastreamGetterTemplate(datastreamId, null, null, (a,b)-> expectedState);
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
