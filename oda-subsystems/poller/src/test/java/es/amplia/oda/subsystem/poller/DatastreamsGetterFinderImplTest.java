package es.amplia.oda.subsystem.poller;

import es.amplia.oda.core.commons.interfaces.DatastreamsGetter;
import es.amplia.oda.core.commons.utils.DatastreamsGettersLocator;
import es.amplia.oda.core.commons.utils.DevicePattern;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatastreamsGetterFinderImplTest {

	DatastreamsGetterFinderImpl datastreamsGetterFinder;

	@Mock
	DatastreamsGettersLocator mockedLocator;
	@Mock
	DatastreamsGetter mockedGetter;

	@Before
	public void prepareForTest() {
		datastreamsGetterFinder = new DatastreamsGetterFinderImpl(mockedLocator);
	}

	@Test
	public void testGetGettersSatisfying() {
		when(mockedLocator.getDatastreamsGetters()).thenReturn(Collections.singletonList(mockedGetter));
		when(mockedGetter.getDatastreamIdSatisfied()).thenReturn("deviceId");
		when(mockedGetter.getDevicesIdManaged()).thenReturn(Collections.singletonList("deviceId"));

		DatastreamsGetterFinder.Return result = datastreamsGetterFinder.getGettersSatisfying(DevicePattern.AllDevicePattern, Collections.singleton("deviceId"));

		assertTrue(result.getGetters().size() > 0);
		assertEquals(0, result.getNotFoundIds().size());
	}

	@Test
	public void testGetGettersSatisfyingWithException() {
		when(mockedLocator.getDatastreamsGetters()).thenReturn(Collections.singletonList(mockedGetter));
		when(mockedGetter.getDatastreamIdSatisfied()).thenThrow(new NullPointerException());

		DatastreamsGetterFinder.Return result = datastreamsGetterFinder.getGettersSatisfying(DevicePattern.AllDevicePattern, Collections.singleton("deviceId"));

		assertEquals(0, result.getGetters().size());
		assertTrue(result.getNotFoundIds().size() > 0);
	}

	@Test (expected = IllegalArgumentException.class)
	public void testGetGettersSatisfyingThrowException() {
		when(mockedLocator.getDatastreamsGetters()).thenReturn(Collections.singletonList(mockedGetter));

		DatastreamsGetterFinder.Return result = datastreamsGetterFinder.getGettersSatisfying(null, Collections.singleton("deviceId"));
	}
}
