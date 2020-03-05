package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.ruleengine.api.*;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RuleEngineNashorn.class)
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
	File mockedFile;

	@Before
	public void setUp() {
		testRuleEngine = new RuleEngineNashorn(mockedScriptTranslator);
	}

	@Test
	public void testLoadConfiguration() throws Exception {
		String root = new File(".").getCanonicalPath();
		RuleEngineConfiguration config = RuleEngineConfiguration.builder().path(root + "/src/test/java/testDirectory").build();
		whenNew(MainDirectoryWatcher.class).withAnyArguments().thenReturn(mockedMainWatcher);
		whenNew(RulesDirectoryWatcher.class).withAnyArguments().thenReturn(mockedRuleWatcher);

		File mainDirToCreate = new File(config.getPath());
		mainDirToCreate.mkdir();
		File ruleDirToCreate = new File(config.getPath() + "/datastreamId");
		ruleDirToCreate.mkdir();
		File ruleFilToCreate = new File(config.getPath() + "/datastreamId/rule.js");
		ruleFilToCreate.createNewFile();
		testRuleEngine.loadConfiguration(config);

		verifyNew(MainDirectoryWatcher.class).withArguments(Paths.get(root + "/src/test/java/testDirectory"), testRuleEngine);
		verifyNew(RulesDirectoryWatcher.class).withArguments(Paths.get(root + "/src/test/java/testDirectory/datastreamId"), testRuleEngine);
		assertNotNull(((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).get(config.getPath() + "/datastreamId/rule.js"));
		ruleFilToCreate.delete();
		ruleDirToCreate.delete();
		mainDirToCreate.delete();
	}

	@Test
	public void testLoadConfigurationRuleException() throws Exception {
		String root = new File(".").getCanonicalPath();
		RuleEngineConfiguration config = RuleEngineConfiguration.builder().path(root + "/src/test/java/testDirectory").build();
		whenNew(MainDirectoryWatcher.class).withAnyArguments().thenReturn(mockedMainWatcher);
		whenNew(RulesDirectoryWatcher.class).withAnyArguments().thenReturn(mockedRuleWatcher);
		whenNew(Rule.class).withAnyArguments().thenThrow(ScriptException.class);

		File mainDirToCreate = new File(config.getPath());
		mainDirToCreate.mkdir();
		File ruleDirToCreate = new File(config.getPath() + "/datastreamId");
		ruleDirToCreate.mkdir();
		File ruleFilToCreate = new File(config.getPath() + "/datastreamId/rule.js");
		ruleFilToCreate.createNewFile();
		testRuleEngine.loadConfiguration(config);

		verifyNew(MainDirectoryWatcher.class).withArguments(Paths.get(root + "/src/test/java/testDirectory"), testRuleEngine);
		verifyNew(RulesDirectoryWatcher.class).withArguments(Paths.get(root + "/src/test/java/testDirectory/datastreamId"), testRuleEngine);
		assertNull(((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).get(config.getPath() + "/datastreamId/rule.js"));
		ruleFilToCreate.delete();
		ruleDirToCreate.delete();
		mainDirToCreate.delete();
	}

	@Test
	public void testEngineNotStartedNotRefreshed() {
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", System.currentTimeMillis(), true, DatastreamValue.Status.OK, "");
		when(mockedState.isRefreshed("testDevice", "testDatastream")).thenReturn(false);

		State state = testRuleEngine.engine(mockedState, value);

		assertEquals(mockedState, state);
		verify(mockedState).refreshValue("testDevice","testDatastream", value);
	}

	@Test
	public void testEngineNotStartedRefreshed() {
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", System.currentTimeMillis(), true, DatastreamValue.Status.OK, "");
		when(mockedState.isRefreshed("testDevice","testDatastream")).thenReturn(true);

		State state = testRuleEngine.engine(mockedState, value);

		assertEquals(mockedState, state);
		verify(mockedState, never()).refreshValue("testDevice","testDatastream", value);
	}

	@Test
	public void testEngineStarted() throws ScriptException {
		String path = "origin/path";
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", System.currentTimeMillis(), true, DatastreamValue.Status.OK, "");
		HashMap<String, Rule> rules = new HashMap<>();
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		Rule rule = new Rule("nameRule", "testDatastream", mockedScriptTranslator);
		rules.put(path + "testDatastream", rule);
		watchers.put(path + "testDatastream", mockedRuleWatcher);
		when(mockedScriptTranslator.runMethod("nameRule", "when", mockedState, value)).thenReturn(true);
		when(mockedScriptTranslator.runMethod("nameRule", "then", mockedState, value)).thenReturn(mockedState);
		when(mockedState.isRefreshed("testDevice","testDatastream")).thenReturn(true);
		Whitebox.setInternalState(testRuleEngine, "started", true);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);
		Whitebox.setInternalState(testRuleEngine, "path", path);

		State state = testRuleEngine.engine(mockedState, value);

		assertEquals(mockedState, state);
		verify(mockedScriptTranslator).runMethod("nameRule", "then", mockedState, value);
	}

	@Test
	public void testEngineStartedException() throws ScriptException {
		String path = "origin/path";
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", System.currentTimeMillis(), true, DatastreamValue.Status.OK, "");
		HashMap<String, Rule> rules = new HashMap<>();
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		Rule rule = new Rule("nameRule", "testDatastream", mockedScriptTranslator);
		rules.put(path + "testDatastream", rule);
		watchers.put("testDatastream", mockedRuleWatcher);
		when(mockedScriptTranslator.runMethod("nameRule", "when", mockedState, value)).thenThrow(new ClassCastException());
		when(mockedState.isRefreshed("testDevice","testDatastream")).thenReturn(true);
		Whitebox.setInternalState(testRuleEngine, "started", true);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);
		Whitebox.setInternalState(testRuleEngine, "path", path);

		State state = testRuleEngine.engine(mockedState, value);

		assertEquals(mockedState, state);
		verify(mockedScriptTranslator, never()).runMethod("nameRule", "then", mockedState, value);
	}

	@Test
	public void testCreateDatastreamDirectory() throws Exception {
		whenNew(File.class).withAnyArguments().thenReturn(mockedFile);
		when(mockedFile.isDirectory()).thenReturn(true);
		Whitebox.setInternalState(testRuleEngine, "watcher", new HashMap<>());

		testRuleEngine.createDatastreamDirectory("testDatastream");

		assertTrue(((HashMap) Whitebox.getInternalState(testRuleEngine, "watcher")).size() > 0);
	}

	@Test
	public void testDeleteDatastreamDirectory() throws ScriptException {
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		HashMap<String, Rule> rules = new HashMap<>();
		Rule rule = new Rule("script", "testDatastream", new NashornScriptTranslator());
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
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);
		String root = new File(".").getCanonicalPath();
		File ruleFilToCreate = new File(root + "/src/test/java/rule.js");
		ruleFilToCreate.createNewFile();

		testRuleEngine.createRule(root + "/src/test/java/rule.js");

		ruleFilToCreate.delete();
		assertTrue(((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).size() > 0);
	}

	@Test
	public void testStop() throws ScriptException {
		Whitebox.setInternalState(testRuleEngine, "started", true);
		HashMap<String, Rule> rules = new HashMap<>();
		HashMap<String, DirectoryWatcher> watchers = new HashMap<>();
		Rule rule = new Rule("nameRule", "testDatastream", mockedScriptTranslator);
		rules.put("rule", rule);
		watchers.put("rule", mockedRuleWatcher);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);
		Whitebox.setInternalState(testRuleEngine, "watcher", watchers);

		testRuleEngine.stop();

		assertEquals(0, ((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).size());
		assertEquals(0, ((HashMap) Whitebox.getInternalState(testRuleEngine, "watcher")).size());
		assertFalse(Whitebox.getInternalState(testRuleEngine, "started"));
	}

	@Test
	public void testDeleteRule() throws ScriptException {
		Whitebox.setInternalState(testRuleEngine, "started", true);
		HashMap<String, Rule> rules = new HashMap<>();
		Rule rule = new Rule("nameRule", "testDatastream", mockedScriptTranslator);
		rules.put("rule", rule);
		Whitebox.setInternalState(testRuleEngine, "rules", rules);

		testRuleEngine.deleteRule("rule");

		assertEquals(0, ((HashMap) Whitebox.getInternalState(testRuleEngine, "rules")).size());
	}
}
