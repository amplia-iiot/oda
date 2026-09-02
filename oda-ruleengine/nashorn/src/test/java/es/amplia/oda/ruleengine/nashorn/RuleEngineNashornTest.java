package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.OsgiContext;
import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.ruleengine.api.*;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.powermock.reflect.Whitebox;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RuleEngineNashornTest {

	@Mock
	ScriptTranslator mockedScriptTranslator;

	@InjectMocks
	RuleEngineNashorn testRuleEngine;

	@Mock
	MainDirectoryWatcher mockedMainWatcher;
	@Mock
	RulesDirectoryWatcher mockedRuleWatcher;
	@Mock
	State mockedState;
	@Mock
	OsgiContext mockedContext;

	@BeforeEach
	public void setUp() {
		testRuleEngine = new RuleEngineNashorn(mockedScriptTranslator);
	}

	@Test
	public void testLoadConfiguration() throws Exception {
		String root = new File(".").getCanonicalPath();
		RuleEngineConfiguration config = RuleEngineConfiguration.builder().path(root + "/src/test/java/testDirectory")
				.utilsPath(root + "/src/test/java/testDirectory").build();
		List<List<?>> mainWatcherArgs = new ArrayList<>();
		List<List<?>> ruleWatcherArgs = new ArrayList<>();
		try (MockedConstruction<MainDirectoryWatcher> mainWatcherConstruction =
					 mockConstruction(MainDirectoryWatcher.class,
							 (mock, mctx) -> mainWatcherArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<RulesDirectoryWatcher> ruleWatcherConstruction =
					 mockConstruction(RulesDirectoryWatcher.class,
							 (mock, mctx) -> ruleWatcherArgs.add(new ArrayList<>(mctx.arguments())))) {

			File mainDirToCreate = new File(config.getPath());
			mainDirToCreate.mkdir();
			File ruleDirToCreate = new File(config.getPath() + "/datastreamId");
			ruleDirToCreate.mkdir();
			File ruleFilToCreate = new File(config.getPath() + "/datastreamId/rule.js");
			ruleFilToCreate.createNewFile();
			testRuleEngine.loadConfiguration(config);

			assertEquals(1, mainWatcherConstruction.constructed().size());
			assertEquals(Paths.get(root + "/src/test/java/testDirectory"), mainWatcherArgs.get(0).get(0));
			assertEquals(testRuleEngine, mainWatcherArgs.get(0).get(1));
			assertEquals(1, ruleWatcherConstruction.constructed().size());
			assertEquals(Paths.get(root + "/src/test/java/testDirectory/datastreamId"), ruleWatcherArgs.get(0).get(0));
			assertEquals(testRuleEngine, ruleWatcherArgs.get(0).get(1));
			assertNotNull(((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).get(config.getPath() + "/datastreamId/rule.js"));
			ruleFilToCreate.delete();
			ruleDirToCreate.delete();
			mainDirToCreate.delete();
		}
	}

	@Test
	public void testLoadConfigurationRuleException() throws Exception {
		String root = new File(".").getCanonicalPath();
		RuleEngineConfiguration config = RuleEngineConfiguration.builder().path(root + "/src/test/java/testDirectory")
				.utilsPath(root + "/src/test/java/testDirectory").build();
		List<List<?>> mainWatcherArgs = new ArrayList<>();
		List<List<?>> ruleWatcherArgs = new ArrayList<>();
		try (MockedConstruction<MainDirectoryWatcher> mainWatcherConstruction =
					 mockConstruction(MainDirectoryWatcher.class,
							 (mock, mctx) -> mainWatcherArgs.add(new ArrayList<>(mctx.arguments())));
			 MockedConstruction<RulesDirectoryWatcher> ruleWatcherConstruction =
					 mockConstruction(RulesDirectoryWatcher.class,
							 (mock, mctx) -> ruleWatcherArgs.add(new ArrayList<>(mctx.arguments())))) {
			doThrow(new ScriptException("test")).when(mockedScriptTranslator).initScript(anyString());

			File mainDirToCreate = new File(config.getPath());
			mainDirToCreate.mkdir();
			File ruleDirToCreate = new File(config.getPath() + "/datastreamId");
			ruleDirToCreate.mkdir();
			File ruleFilToCreate = new File(config.getPath() + "/datastreamId/rule.js");
			ruleFilToCreate.createNewFile();
			testRuleEngine.loadConfiguration(config);

			assertEquals(1, mainWatcherConstruction.constructed().size());
			assertEquals(Paths.get(root + "/src/test/java/testDirectory"), mainWatcherArgs.get(0).get(0));
			assertEquals(testRuleEngine, mainWatcherArgs.get(0).get(1));
			assertEquals(1, ruleWatcherConstruction.constructed().size());
			assertEquals(Paths.get(root + "/src/test/java/testDirectory/datastreamId"), ruleWatcherArgs.get(0).get(0));
			assertEquals(testRuleEngine, ruleWatcherArgs.get(0).get(1));
			assertNull(((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).get(config.getPath() + "/datastreamId/rule.js"));
			ruleFilToCreate.delete();
			ruleDirToCreate.delete();
			mainDirToCreate.delete();
		}
	}

	@Test
	public void testEngineNotStartedRefreshed() {
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);
		when(mockedState.isRefreshed("testDevice","testDatastream")).thenReturn(true);

		State state = testRuleEngine.engine(mockedState, value, mockedContext);

		assertEquals(mockedState, state);
		verify(mockedState, never()).refreshValue("testDevice","testDatastream", value);
    }

	@Test
	public void testEngineStarted() throws ScriptException {
		String path = "origin/path/";
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);
		HashMap<String, Rule> rules = new HashMap<>();
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		Rule rule = new Rule("nameRule", Collections.singletonList("testDatastream"), mockedScriptTranslator);
		rules.put("nameRule", rule);
		watchers.put(path + "testDatastream", mockedRuleWatcher);
		when(mockedScriptTranslator.runMethod("nameRule", "when", mockedState, value, mockedContext)).thenReturn(true);
		when(mockedScriptTranslator.runMethod("nameRule", "then", mockedState, value, mockedContext)).thenReturn(mockedState);
		when(mockedState.isRefreshed("testDevice","testDatastream")).thenReturn(true);
		Whitebox.setInternalState(testRuleEngine, "started", true);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);
		Whitebox.setInternalState(testRuleEngine, "path", path);

		State state = testRuleEngine.engine(mockedState, value, mockedContext);

		assertEquals(mockedState, state);
		verify(mockedScriptTranslator).runMethod("nameRule", "then", mockedState, value, mockedContext);
	}

	@Test
	public void testEngineStartedException() throws ScriptException {
		String path = "origin/path";
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);
		HashMap<String, Rule> rules = new HashMap<>();
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		Rule rule = new Rule(path + "testDatastream", Collections.singletonList("testDatastream"), mockedScriptTranslator);
		rules.put(path + "testDatastream", rule);
		watchers.put("testDatastream", mockedRuleWatcher);
		when(mockedScriptTranslator.runMethod(path + "testDatastream", "when", mockedState, value, mockedContext)).thenThrow(new ClassCastException());
		when(mockedState.isRefreshed("testDevice","testDatastream")).thenReturn(true);
		Whitebox.setInternalState(testRuleEngine, "started", true);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);
		Whitebox.setInternalState(testRuleEngine, "path", path);

		State state = testRuleEngine.engine(mockedState, value, mockedContext);

		assertEquals(mockedState, state);
		verify(mockedScriptTranslator, never()).runMethod("nameRule", "then", mockedState, value, mockedContext);
	}

	@Test
	public void testCreateDatastreamDirectory() throws Exception {
		Whitebox.setInternalState(testRuleEngine, "watcher", new HashMap<>());

		try (MockedConstruction<File> fileConstruction = mockConstruction(File.class,
				(mock, mctx) -> when(mock.isDirectory()).thenReturn(true))) {
			testRuleEngine.createDatastreamDirectory("testDatastream");
		}

		assertTrue(((HashMap) Whitebox.getInternalState(testRuleEngine, "watcher")).size() > 0);
	}

	@Test
	public void testDeleteDatastreamDirectory() throws ScriptException, IOException {
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		HashMap<String, Rule> rules = new HashMap<>();
		Rule rule = new Rule("script", Collections.singletonList("testDatastream"), mockedScriptTranslator);
		watchers.put("script", mockedRuleWatcher);
		rules.put("script", rule);
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);
		Whitebox.setInternalState(testRuleEngine, "path", "");

		testRuleEngine.deleteDatastreamDirectory("script");

		assertEquals(0, ((HashMap)Whitebox.getInternalState(testRuleEngine, "watcher")).size());
		assertEquals(0, ((HashMap)Whitebox.getInternalState(testRuleEngine, "rules")).size());
	}

	@Test
	public void testCreateRule() throws IOException {
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		HashMap<String, Rule> rules = new HashMap<>();
		String root = new File(".").getCanonicalPath() + "/src/test/java/";
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);
		Whitebox.setInternalState(testRuleEngine, "path", root);

		File ruleFilToCreate = new File(root + "rule.js");
		ruleFilToCreate.createNewFile();

		testRuleEngine.createRule(root + "rule.js");

		ruleFilToCreate.delete();
		assertTrue(((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).size() > 0);
	}

	@Test
	public void testStop() throws ScriptException {
		Whitebox.setInternalState(testRuleEngine, "started", true);
		HashMap<String, Rule> rules = new HashMap<>();
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		Rule rule = new Rule("nameRule", Collections.singletonList("testDatastream"), mockedScriptTranslator);
		rules.put("rule", rule);
		watchers.put("rule", mockedRuleWatcher);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);

		testRuleEngine.stop();

		assertEquals(0, ((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).size());
		assertEquals(0, ((HashMap) Whitebox.getInternalState(testRuleEngine, "watcher")).size());
		assertFalse((boolean) Whitebox.getInternalState(testRuleEngine, "started"));
	}

	@Test
	public void testDeleteRule() throws ScriptException {
		Whitebox.setInternalState(testRuleEngine, "started", true);
		HashMap<String, Rule> rules = new HashMap<>();
		Rule rule = new Rule("nameRule", Collections.singletonList("testDatastream"), mockedScriptTranslator);
		rules.put("rule", rule);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);

		testRuleEngine.deleteRule("rule");

		assertEquals(0, ((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).size());
	}

	@Test
	public void testRuleMultipleDatastreams() throws IOException {

		// rules directory for test
		String root = new File(".").getCanonicalPath() + "/src/test/java/rules/";
		String datastreamId = "tenA";

		// datastream value that will trigger the rules
		DatastreamValue value = new DatastreamValue("testDevice", datastreamId, "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);

		// create directory watcher
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		watchers.put(root + datastreamId, mockedRuleWatcher);

		when(mockedState.isRefreshed("testDevice", datastreamId)).thenReturn(true);
		Whitebox.setInternalState(testRuleEngine, "started", true);
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);
		Whitebox.setInternalState(testRuleEngine, "path", root);

		// create and store rules
		HashMap<String, Rule> rules = new HashMap<>();
		Whitebox.setInternalState(testRuleEngine, "rules", rules);

		// rule with datastream equal to the datastream value, must be executed
		String rule1Path = root + "tenA";
		Files.createDirectories(Paths.get(rule1Path));
		String rule1Name = rule1Path + "/rule1.js";
		File rule1File = new File(rule1Name);
		rule1File.createNewFile();
		testRuleEngine.createRule(rule1Name);
		// rule with datastream that contains the datastream value, doesn't have to be executed
		String rule2Path = root + "tenAB";
		Files.createDirectories(Paths.get(rule2Path));
		String rule2Name = rule2Path + "/rule2.js";
		File rule2File = new File(rule2Name);
		rule2File.createNewFile();
		testRuleEngine.createRule(rule2Name);
		// rule with datastream that contains the datastream value, doesn't have to be executed
		String rule3Path = root + "tenAC";
		Files.createDirectories(Paths.get(rule3Path));
		String rule3Name = rule3Path + "/rule3.js";
		File rule3File = new File(rule3Name);
		rule3File.createNewFile();
		testRuleEngine.createRule(rule3Name);
		// rule with multiple datastreamIds, one of them is equal to the datastream value, must be executed
		String rule4Path = root + "tenA:tenAB";
		Files.createDirectories(Paths.get(rule4Path));
		String rule4Name = rule4Path + "/rule4.js";
		File rule4File = new File(rule4Name);
		rule4File.createNewFile();
		testRuleEngine.createRule(rule4Name);

		// test conditions
		when(mockedScriptTranslator.runMethod(rule1Name, "when", mockedState, value, mockedContext)).thenReturn(true);
		when(mockedScriptTranslator.runMethod(rule1Name, "then", mockedState, value, mockedContext)).thenReturn(mockedState);
		when(mockedScriptTranslator.runMethod(rule4Name, "when", mockedState, value, mockedContext)).thenReturn(true);
		when(mockedScriptTranslator.runMethod(rule4Name, "then", mockedState, value, mockedContext)).thenReturn(mockedState);

		// launch method
		testRuleEngine.engine(mockedState, value, mockedContext);

		// assertions
		// check rule 1 is applied
		verify(mockedScriptTranslator).runMethod(rule1Name, "then", mockedState, value, mockedContext);
		// check rule 2 is not applied
		verify(mockedScriptTranslator, never()).runMethod(rule2Name, "when", mockedState, value, mockedContext);
		// check rule 3 is not applied
		verify(mockedScriptTranslator, never()).runMethod(rule3Name, "when", mockedState, value, mockedContext);
		// check rule 4 is applied
		verify(mockedScriptTranslator).runMethod(rule4Name, "then", mockedState, value, mockedContext);

		// clean files created
		rule1File.delete();
		rule2File.delete();
		rule3File.delete();
		rule4File.delete();
		Files.delete(Paths.get(rule1Path));
		Files.delete(Paths.get(rule2Path));
		Files.delete(Paths.get(rule3Path));
		Files.delete(Paths.get(rule4Path));
		Files.delete(Paths.get(root));
	}

	@Test
	public void testReloadAllRules() throws IOException {

		String root = new File(".").getCanonicalPath() + "/src/test/java/rules/";

		// initial conditions
		Whitebox.setInternalState(testRuleEngine, "path", root);
		// create and store rules
		HashMap<String, Rule> rules = new HashMap<>();
		Whitebox.setInternalState(testRuleEngine, "rules", rules);


		// rule1
		String rule1Path = root + "rule1";
		Files.createDirectories(Paths.get(rule1Path));
		String rule1Name = rule1Path + "/rule1.js";
		File rule1File = new File(rule1Name);
		rule1File.createNewFile();
		// rule2
		String rule2Path = root + "rule2";
		Files.createDirectories(Paths.get(rule2Path));
		String rule2Name = rule2Path + "/rule2.js";
		File rule2File = new File(rule2Name);
		rule2File.createNewFile();

		// test before method call
		rules = Whitebox.getInternalState(testRuleEngine, "rules");
		assertEquals(0, rules.size());

		// call method to test
		testRuleEngine.reloadAllRules();

		// assertions
		rules = Whitebox.getInternalState(testRuleEngine, "rules");
		assertEquals(2, rules.size());

		// clean files created
		rule1File.delete();
		rule2File.delete();
		Files.delete(Paths.get(rule1Path));
		Files.delete(Paths.get(rule2Path));
		Files.delete(Paths.get(root));
	}
}
