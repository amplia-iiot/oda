package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.State;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.osgi.framework.BundleContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RuleEngineProxy.class)
public class RuleEngineProxyTest {

	private static final String TEST_NAME_RULE = "nameRule";
	private static final DatastreamValue TEST_DATASTREAM_VALUE =
			new DatastreamValue("testDevice", "testDatastream", System.currentTimeMillis(),
					true, DatastreamValue.Status.OK, "", false, false);

	@Mock
	private BundleContext mockedContext;
	private RuleEngineProxy testProxy;

	@Mock
	private OsgiServiceProxy<RuleEngine> mockedProxy;
	@Mock
	private RuleEngine mockedRuleEngine;
	@Mock
	private State mockedState;
	@Captor
	private ArgumentCaptor<Function<RuleEngine, CompletableFuture<DatastreamValue>>>
			datastreamValueFutureFunctionCaptor;
	@Captor
	private ArgumentCaptor<Consumer<RuleEngine>> ruleEngineConsumerCaptor;


	@Before
	public void setUp() throws Exception {
		PowerMockito.whenNew(OsgiServiceProxy.class).withAnyArguments().thenReturn(mockedProxy);

		testProxy = new RuleEngineProxy(mockedContext);
	}

	@Test
	public void testConstructor() throws Exception {
		PowerMockito.verifyNew(OsgiServiceProxy.class).withArguments(eq(RuleEngine.class), eq(mockedContext));
	}

	@Test
	public void testEngine() {
		testProxy.engine(mockedState, TEST_DATASTREAM_VALUE);

		verify(mockedProxy).callFirst(datastreamValueFutureFunctionCaptor.capture());
		Function<RuleEngine, CompletableFuture<DatastreamValue>> capturedFunction =
				datastreamValueFutureFunctionCaptor.getValue();
		capturedFunction.apply(mockedRuleEngine);
		verify(mockedRuleEngine).engine(mockedState, TEST_DATASTREAM_VALUE);
	}

	@Test
	public void testCreateDatastreamDirectory() {
		testProxy.createDatastreamDirectory(TEST_NAME_RULE);

		verify(mockedProxy).consumeFirst(ruleEngineConsumerCaptor.capture());
		Consumer<RuleEngine> capturedFunction =
				ruleEngineConsumerCaptor.getValue();
		capturedFunction.accept(mockedRuleEngine);
		verify(mockedRuleEngine).createDatastreamDirectory(TEST_NAME_RULE);
	}

	@Test
	public void testDeleteDatastreamDirectory() {
		testProxy.deleteDatastreamDirectory(TEST_NAME_RULE);

		verify(mockedProxy).consumeFirst(ruleEngineConsumerCaptor.capture());
		Consumer<RuleEngine> capturedFunction =
				ruleEngineConsumerCaptor.getValue();
		capturedFunction.accept(mockedRuleEngine);
		verify(mockedRuleEngine).deleteDatastreamDirectory(TEST_NAME_RULE);
	}

	@Test
	public void testCreateRule() {
		testProxy.createRule(TEST_NAME_RULE);

		verify(mockedProxy).consumeFirst(ruleEngineConsumerCaptor.capture());
		Consumer<RuleEngine> capturedFunction =
				ruleEngineConsumerCaptor.getValue();
		capturedFunction.accept(mockedRuleEngine);
		verify(mockedRuleEngine).createRule(TEST_NAME_RULE);
	}

	@Test
	public void testDeleteRule() {
		testProxy.deleteRule(TEST_NAME_RULE);

		verify(mockedProxy).consumeFirst(ruleEngineConsumerCaptor.capture());
		Consumer<RuleEngine> capturedFunction =
				ruleEngineConsumerCaptor.getValue();
		capturedFunction.accept(mockedRuleEngine);
		verify(mockedRuleEngine).deleteRule(TEST_NAME_RULE);
	}

	@Test
	public void testReloadAllRules() {
		testProxy.reloadAllRules();

		verify(mockedProxy).consumeFirst(ruleEngineConsumerCaptor.capture());
		Consumer<RuleEngine> capturedFunction = ruleEngineConsumerCaptor.getValue();
		capturedFunction.accept(mockedRuleEngine);
		verify(mockedRuleEngine).reloadAllRules();
	}

	@Test
	public void testStop() {
		testProxy.stop();

		verify(mockedProxy).consumeFirst(ruleEngineConsumerCaptor.capture());
		Consumer<RuleEngine> capturedFunction =
				ruleEngineConsumerCaptor.getValue();
		capturedFunction.accept(mockedRuleEngine);
		verify(mockedRuleEngine).stop();
	}

	@Test
	public void testStart() {
		testProxy.close();

		verify(mockedProxy).close();
	}
}
