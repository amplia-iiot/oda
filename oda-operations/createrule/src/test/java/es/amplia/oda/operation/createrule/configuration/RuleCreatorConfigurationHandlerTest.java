package es.amplia.oda.operation.createrule.configuration;

import es.amplia.oda.core.commons.exceptions.ConfigurationException;
import es.amplia.oda.operation.createrule.OperationCreateRuleImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Dictionary;
import java.util.Hashtable;

import static es.amplia.oda.operation.createrule.configuration.RuleCreatorConfigurationHandler.PATH_PROPERTY_NAME;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RuleCreatorConfigurationHandler.class)
public class RuleCreatorConfigurationHandlerTest {

	@Mock
	private OperationCreateRuleImpl mockedOperation;

	private RuleCreatorConfigurationHandler ruleCreatorConfigurationHandler;
	private static final String path = "this/is/a/path";

	@Before
	public void prepareForTest() {
		ruleCreatorConfigurationHandler = new RuleCreatorConfigurationHandler(mockedOperation);
	}

	@Test
	public void testLoadConfiguration() {
		Dictionary<String, String> props = new Hashtable<>();
		props.put(PATH_PROPERTY_NAME, path);

		ruleCreatorConfigurationHandler.loadConfiguration(props);

		assertEquals(path, ((RuleCreatorConfiguration)Whitebox.getInternalState(ruleCreatorConfigurationHandler, "config")).getPath());
	}

	@Test
	public void testApplyConfiguration() {
		RuleCreatorConfiguration config = RuleCreatorConfiguration.builder().path(path).build();
		Whitebox.setInternalState(ruleCreatorConfigurationHandler, "config", config);

		ruleCreatorConfigurationHandler.applyConfiguration();

		verify(mockedOperation).loadConfiguration(config);
	}

	@Test
	public void testMissingPathExceptionSupplier() {
		assertThat(ruleCreatorConfigurationHandler.missingPathExceptionSupplier().get(), instanceOf(ConfigurationException.class));
	}
}
