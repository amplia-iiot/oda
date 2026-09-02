package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.osgi.proxies.OsgiServiceProxy;
import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.OsgiContext;
import es.amplia.oda.core.commons.utils.State;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RuleEngineProxyTest {

	private static final String TEST_NAME_RULE = "nameRule";
	private static final DatastreamValue TEST_DATASTREAM_VALUE =
			new DatastreamValue("testDevice", "testDatastream", "feed", System.currentTimeMillis(),
					true, DatastreamValue.Status.OK, "", false, false);

	@Mock
	private BundleContext mockedContext;
	private RuleEngineProxy testProxy;

	private MockedConstruction<OsgiServiceProxy> proxyConstruction;
	private final List<List<?>> proxyArgs = new ArrayList<>();
	private OsgiServiceProxy<RuleEngine> mockedProxy;
	@Mock
	private RuleEngine mockedRuleEngine;
	@Mock
	private State mockedState;
	@Mock
	OsgiContext mockedOsgiContext;
	@Captor
	private ArgumentCaptor<Function<RuleEngine, CompletableFuture<DatastreamValue>>>
			datastreamValueFutureFunctionCaptor;
	@Captor
	private ArgumentCaptor<Consumer<RuleEngine>> ruleEngineConsumerCaptor;


	@BeforeEach
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		proxyConstruction = mockConstruction(OsgiServiceProxy.class,
				(mock, mctx) -> proxyArgs.add(new ArrayList<>(mctx.arguments())));

		testProxy = new RuleEngineProxy(mockedContext);
		mockedProxy = proxyConstruction.constructed().get(0);
	}

	@AfterEach
	public void tearDown() {
		proxyConstruction.close();
	}

	@Test
	public void testConstructor() throws Exception {
		assertEquals(1, proxyConstruction.constructed().size());
		assertEquals(RuleEngine.class, proxyArgs.get(0).get(0));
		assertEquals(mockedContext, proxyArgs.get(0).get(1));
	}

	@Test
	public void testEngine() {
		testProxy.engine(mockedState, TEST_DATASTREAM_VALUE, mockedOsgiContext);

		verify(mockedProxy).callFirst(datastreamValueFutureFunctionCaptor.capture());
		Function<RuleEngine, CompletableFuture<DatastreamValue>> capturedFunction =
				datastreamValueFutureFunctionCaptor.getValue();
		capturedFunction.apply(mockedRuleEngine);
		verify(mockedRuleEngine).engine(mockedState, TEST_DATASTREAM_VALUE, mockedOsgiContext);
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
