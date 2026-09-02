package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;

import es.amplia.oda.datastreams.i2c.datastreams.I2CDatastreamsFactoryImpl;
import es.amplia.oda.datastreams.i2c.datastreams.I2CDatastreamsGetter;
import es.amplia.oda.datastreams.i2c.datastreams.I2CDatastreamsSetter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mockConstruction;

@RunWith(MockitoJUnitRunner.Silent.class)
public class I2CDatastreamsFactoryImplTest {

	private static final String TEST_NAME = "datastreamId";
	private static final String TEST_DEFAULT_DEVICE_NAME = "defaultDeviceName";
	private static final long TEST_MIN = 1;
	private static final long TEST_MAX = 50;


	@Mock
	private I2CService mockedService;
	@InjectMocks
	private I2CDatastreamsFactoryImpl testFactory;


	@Test
	public void testCreateDatastreamsGetter() {
		List<List<?>> getterArgs = new ArrayList<>();
		try (MockedConstruction<I2CDatastreamsGetter> getterCons = mockConstruction(I2CDatastreamsGetter.class,
				(mock, mctx) -> getterArgs.add(new ArrayList<>(mctx.arguments())))) {

			testFactory.createDatastreamsGetter(TEST_NAME, TEST_DEFAULT_DEVICE_NAME, TEST_MIN, TEST_MAX);

			assertEquals(1, getterCons.constructed().size());
			assertEquals(TEST_NAME, getterArgs.get(0).get(0));
			assertEquals(TEST_DEFAULT_DEVICE_NAME, getterArgs.get(0).get(1));
			assertEquals(TEST_MIN, getterArgs.get(0).get(2));
			assertEquals(TEST_MAX, getterArgs.get(0).get(3));
			assertEquals(mockedService, getterArgs.get(0).get(4));
		}
	}

	@Test
	public void testCreateDatastreamsSetter() {
		List<List<?>> setterArgs = new ArrayList<>();
		try (MockedConstruction<I2CDatastreamsSetter> setterCons = mockConstruction(I2CDatastreamsSetter.class,
				(mock, mctx) -> setterArgs.add(new ArrayList<>(mctx.arguments())))) {

			testFactory.createDatastreamsSetter(TEST_NAME);

			assertEquals(1, setterCons.constructed().size());
			assertEquals(TEST_NAME, setterArgs.get(0).get(0));
			assertEquals(mockedService, setterArgs.get(0).get(1));
		}
	}
}
