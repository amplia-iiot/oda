package es.amplia.oda.datastreams.i2c;

import es.amplia.oda.core.commons.i2c.I2CService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.Executor;

import static org.junit.Assert.*;

@RunWith(PowerMockRunner.class)
public class I2CDatastreamFactoryTest {

	private final String name = "datastreamId";
	@Mock
	I2CService mockedService;
	@Mock
	Executor mockedExecutor;

	@Test
	public void testCreateDatastreamsGetter() {
		I2CDatastreamsGetter getter = I2CDatastreamFactory.createDatastreamsGetter(name, mockedService, mockedExecutor);

		assertNotNull(getter);
	}

	@Test
	public void testCreateDatastreamsSetter() {
		I2CDatastreamsSetter setter = I2CDatastreamFactory.createDatastreamsSetter(name, mockedService, mockedExecutor);

		assertNotNull(setter);
	}
}