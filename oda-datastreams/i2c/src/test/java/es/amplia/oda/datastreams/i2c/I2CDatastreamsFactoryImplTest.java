package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Matchers.eq;

@RunWith(PowerMockRunner.class)
@PrepareForTest(I2CDatastreamsFactoryImpl.class)
public class I2CDatastreamsFactoryImplTest {

	private static final String TEST_NAME = "datastreamId";
	private static final long TEST_MIN = 1;
	private static final long TEST_MAX = 50;


	@Mock
	private I2CService mockedService;
	@InjectMocks
	private I2CDatastreamsFactoryImpl testFactory;

	@Mock
	private I2CDatastreamsGetter mockedGetter;
	@Mock
	private I2CDatastreamsSetter mockedSetter;


	@Test
	public void testCreateDatastreamsGetter() throws Exception {
		PowerMockito.whenNew(I2CDatastreamsGetter.class).withAnyArguments().thenReturn(mockedGetter);

		testFactory.createDatastreamsGetter(TEST_NAME, TEST_MIN, TEST_MAX);

		PowerMockito.verifyNew(I2CDatastreamsGetter.class).withArguments(eq(TEST_NAME), eq(TEST_MIN), eq(TEST_MAX),
				eq(mockedService));
	}

	@Test
	public void testCreateDatastreamsSetter() throws Exception {
		PowerMockito.whenNew(I2CDatastreamsSetter.class).withAnyArguments().thenReturn(mockedSetter);

		testFactory.createDatastreamsSetter(TEST_NAME);

		PowerMockito.verifyNew(I2CDatastreamsSetter.class).withArguments(eq(TEST_NAME), eq(mockedService));
	}
}