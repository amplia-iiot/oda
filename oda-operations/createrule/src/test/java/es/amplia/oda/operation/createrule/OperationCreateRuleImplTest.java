package es.amplia.oda.operation.createrule;

import es.amplia.oda.operation.api.OperationCreateRule;
import es.amplia.oda.operation.createrule.configuration.RuleCreatorConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(OperationCreateRuleImpl.class)
public class OperationCreateRuleImplTest {

	OperationCreateRuleImpl createRule = new OperationCreateRuleImpl();

	@Mock
	FileOutputStream mockedStream;

	@Test
	public void testCreateRuleNotExistentDatastreamCorrectly() throws ExecutionException, InterruptedException {
		Whitebox.setInternalState(createRule, "path", System.getProperty("user.dir") + "/src/test/");
		String deviceId = "dev";
		String namerule = "testRule";
		String datastreamId = "game";
		String when = "return true;";
		String then = "return state;";
		Map<String, String> ruleInfo = new HashMap<>();
		ruleInfo.put("namerule", namerule);
		ruleInfo.put("datastreamId", datastreamId);
		ruleInfo.put("when", when);
		ruleInfo.put("then", then);

		CompletableFuture<OperationCreateRule.Result> futureResult = createRule.createRule(deviceId, ruleInfo);

		futureResult.get().getResultCode().equals(OperationCreateRule.Status.OK);
		File file = new File (System.getProperty("user.dir") + "/src/test/" + datastreamId + FileSystems.getDefault().getSeparator() + namerule + ".js");
		assertTrue(file.exists());
		file.delete();
		File dir = new File (System.getProperty("user.dir") + "/src/test/" + datastreamId);
		dir.delete();
	}

	@Test
	public void testCreateRuleExistentDatastreamCorrectly() throws ExecutionException, InterruptedException {
		Whitebox.setInternalState(createRule, "path", System.getProperty("user.dir") + "/src/test/");
		String deviceId = "dev";
		String namerule = "testRule";
		String datastreamId = "game";
		String when = "return true;";
		String then = "return state;";
		Map<String, String> ruleInfo = new HashMap<>();
		File dir = new File (System.getProperty("user.dir") + "/src/test/" + datastreamId);
		if(!dir.exists()) {
			dir.mkdir();
		}
		ruleInfo.put("namerule", namerule);
		ruleInfo.put("datastreamId", datastreamId);
		ruleInfo.put("when", when);
		ruleInfo.put("then", then);

		CompletableFuture<OperationCreateRule.Result> futureResult = createRule.createRule(deviceId, ruleInfo);

		futureResult.get().getResultCode().equals(OperationCreateRule.Status.OK);
		File file = new File (System.getProperty("user.dir") + "/src/test/" + datastreamId + FileSystems.getDefault().getSeparator() + namerule + ".js");
		assertTrue(file.exists());
		file.delete();
		dir.delete();
	}

	@Test
	public void testCreateRuleExistentRule() throws ExecutionException, InterruptedException, IOException {
		Whitebox.setInternalState(createRule, "path", System.getProperty("user.dir") + "/src/test/");
		String deviceId = "dev";
		String namerule = "testRule";
		String datastreamId = "game";
		String when = "return true;";
		String then = "return state;";
		Map<String, String> ruleInfo = new HashMap<>();
		File dir = new File (System.getProperty("user.dir") + "/src/test/" + datastreamId);
		if(!dir.exists()) {
			dir.mkdir();
		}
		File file = new File (System.getProperty("user.dir") + "/src/test/" + datastreamId + FileSystems.getDefault().getSeparator() + namerule + ".js");
		if(!file.exists()) {
			file.createNewFile();
		}
		ruleInfo.put("namerule", namerule);
		ruleInfo.put("datastreamId", datastreamId);
		ruleInfo.put("when", when);
		ruleInfo.put("then", then);

		CompletableFuture<OperationCreateRule.Result> futureResult = createRule.createRule(deviceId, ruleInfo);

		futureResult.get().getResultCode().equals(OperationCreateRule.Status.ALREADY_EXISTS);
		assertTrue(file.exists());
		file.delete();
		dir.delete();
	}

	@Test
	public void testBadCreateRuleExistentDatastream() throws Exception {
		Whitebox.setInternalState(createRule, "path", System.getProperty("user.dir") + "/src/test/");
		String deviceId = "dev";
		String namerule = "testRule";
		String datastreamId = "game";
		String when = "return true;";
		String then = "return state;";
		Map<String, String> ruleInfo = new HashMap<>();
		File dir = new File (System.getProperty("user.dir") + "/src/test/" + datastreamId);
		if(!dir.exists()) {
			dir.mkdir();
		}
		ruleInfo.put("namerule", namerule);
		ruleInfo.put("datastreamId", datastreamId);
		ruleInfo.put("when", when);
		ruleInfo.put("then", then);
		whenNew(FileOutputStream.class).withArguments(System.getProperty("user.dir") + "/src/test/" + datastreamId + "/" + namerule + ".js").thenReturn(mockedStream);
		doThrow(new IOException()).when(mockedStream).write(any());

		CompletableFuture<OperationCreateRule.Result> futureResult = createRule.createRule(deviceId, ruleInfo);

		futureResult.get().getResultCode().equals(OperationCreateRule.Status.ERROR_CREATING);
		dir.delete();
	}

	@Test
	public void testBadCreateRuleNotExistentDatastream() throws Exception {
		Whitebox.setInternalState(createRule, "path", System.getProperty("user.dir") + "/src/test/");
		String deviceId = "dev";
		String namerule = "testRule";
		String datastreamId = "game";
		String when = "return true;";
		String then = "return state;";
		Map<String, String> ruleInfo = new HashMap<>();
		ruleInfo.put("namerule", namerule);
		ruleInfo.put("datastreamId", datastreamId);
		ruleInfo.put("when", when);
		ruleInfo.put("then", then);
		whenNew(FileOutputStream.class).withArguments(System.getProperty("user.dir") + "/src/test/" + datastreamId + "/" + namerule + ".js").thenReturn(mockedStream);
		doThrow(new IOException()).when(mockedStream).write(any());

		CompletableFuture<OperationCreateRule.Result> futureResult = createRule.createRule(deviceId, ruleInfo);

		futureResult.get().getResultCode().equals(OperationCreateRule.Status.ERROR_CREATING);
		File dir = new File (System.getProperty("user.dir") + "/src/test/" + datastreamId);
		dir.delete();
	}

	@Test
	public void testLoadConfiguration() {
		String path = System.getProperty("user.dir") + "/src/test/";
		RuleCreatorConfiguration config = RuleCreatorConfiguration.builder().path(path).build();

		createRule.loadConfiguration(config);

		assertEquals(path, Whitebox.getInternalState(createRule, "path"));
	}
}
