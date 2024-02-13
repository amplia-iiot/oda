package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

@RunWith(PowerMockRunner.class)
@PrepareForTest(NashornScriptTranslator.class)
@PowerMockIgnore("jdk.internal.reflect.*")
public class NashornScriptTranslatorTest {

	NashornScriptTranslator testTranslator;

	@Mock
	ScriptEngineManager mockedManager;
	@Mock
	ScriptEngine mockedEngine;
	@Mock
	State mockedState;
	@Mock
	Invocable mockedInvocable;

	@Before
	public void setUp() {
		testTranslator = new NashornScriptTranslator();
	}

	@Test
	public void testInitScript() throws Exception {
		whenNew(ScriptEngineManager.class).withAnyArguments().thenReturn(mockedManager);
		when(mockedManager.getEngineByName(any())).thenReturn(mockedEngine);
		String root = new File(".").getCanonicalPath();
		Whitebox.setInternalState(testTranslator, "jsUtilsPath", root + "/src/test/");
		File ruleFilToCreate = new File(root + "/src/test/rule.js");
		ruleFilToCreate.createNewFile();
		FileOutputStream output = new FileOutputStream(root + "/src/test/rule.js");
		output.write(42);

		String script = root + "/src/test/rule.js";
		testTranslator.initScript(script);

		verify(mockedEngine).eval("*");
		assertTrue(((HashMap) Whitebox.getInternalState(testTranslator, "engines")).size() > 0);
		output.close();
		ruleFilToCreate.delete();
	}

	@Test
	public void testInitScriptFileNotExists() throws Exception {
		whenNew(ScriptEngineManager.class).withAnyArguments().thenReturn(mockedManager);
		when(mockedManager.getEngineByName(any())).thenReturn(mockedEngine);

		String script = "none file to do the test";
		testTranslator.initScript(script);

		verify(mockedEngine).eval("");
		assertTrue(((HashMap) Whitebox.getInternalState(testTranslator, "engines")).size() > 0);
	}

	@Test
	public void testClose() {
		testTranslator.close();

		assertEquals(0, ((HashMap) Whitebox.getInternalState(testTranslator, "engines")).size());
	}

	@Test
	public void testPutAttribute() throws Exception {
		whenNew(ScriptEngineManager.class).withAnyArguments().thenReturn(mockedManager);
		when(mockedManager.getEngineByName(any())).thenReturn(mockedEngine);
		HashMap<String, ScriptEngine> map = new HashMap<>();
		map.put("rule.js", mockedEngine);
		Whitebox.setInternalState(testTranslator, "engines", map);

		testTranslator.putAttribute("rule.js", "att", true);

		assertTrue(((HashMap) Whitebox.getInternalState(testTranslator, "engines")).size() > 0);
	}

	@Test
	public void testRunMethod() throws ScriptException {
		HashMap<String, ScriptEngine> map = new HashMap<>();
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		engine.eval("function nothing(a, b) {return a}");
		map.put("rule.js", engine);
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);
		Whitebox.setInternalState(testTranslator, "engines", map);

		State state = (State) testTranslator.runMethod("rule.js", "nothing", mockedState, value);

		assertEquals(mockedState, state);
	}

	@Test
	public void testRunMethodScriptException() throws ScriptException {
		HashMap<String, ScriptEngine> map = new HashMap<>();
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		engine.eval("function nothing(a, b) {throw new ScriptException(\"Jej\")}");
		map.put("rule.js", engine);
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);
		Whitebox.setInternalState(testTranslator, "engines", map);

		State state = (State) testTranslator.runMethod("rule.js", "nothing", mockedState, value);

		assertEquals(mockedState, state);
	}

	@Test
	public void testRunMethodNoMethodException() throws ScriptException {
		HashMap<String, ScriptEngine> map = new HashMap<>();
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn");
		engine.eval("");
		map.put("rule.js", engine);
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);
		Whitebox.setInternalState(testTranslator, "engines", map);

		State state = (State) testTranslator.runMethod("rule.js", "nothing", mockedState, value);

		assertEquals(mockedState, state);
	}

	@Test
	public void testReplaceLoadPath() throws ScriptException, IOException {

		String root = new File(".").getCanonicalPath() + "/src/test/java/rules/";
		Whitebox.setInternalState(testTranslator, "jsUtilsPath", root + "jsUtils/");

		// rule file
		String rulePath = root + "rule";
		Files.createDirectories(Paths.get(rulePath));
		String ruleName = rulePath + "/rule.js";
		File ruleFile = new File(ruleName);
		ruleFile.createNewFile();
		FileWriter fileWriter = new FileWriter(ruleName);
		fileWriter.write("load(\"utils.js\");\n function nothing(a, b) \n {return a}");
		fileWriter.close();

		// js utils file
		String jsUtilsPath = root + "/jsUtils";
		Files.createDirectories(Paths.get(jsUtilsPath));
		String jsUtilsName = jsUtilsPath + "/utils.js";
		File jsUtilsFile = new File(jsUtilsName);
		jsUtilsFile.createNewFile();

		// launch method
		testTranslator.initScript(ruleName);

		// test condition
		assertTrue(((HashMap) Whitebox.getInternalState(testTranslator, "engines")).size() > 0);

		// clean files created
		ruleFile.delete();
		jsUtilsFile.delete();
		Files.delete(Paths.get(rulePath));
		Files.delete(Paths.get(jsUtilsPath));
		Files.delete(Paths.get(root));
	}

	@Test
	public void testLoadConfiguration() throws Exception {
		String root = new File(".").getCanonicalPath() + "/src/test/java/rules/";
		RuleEngineConfiguration config = RuleEngineConfiguration.builder().path(root + "/src/test/java/rules/testDirectory")
				.utilsPath(root + "/src/test/java/rules/testDirectory").build();

		testTranslator.loadConfiguration(config);

		assertEquals(Whitebox.getInternalState(testTranslator, "jsUtilsPath"), root + "/src/test/java/rules/testDirectory");
	}
}
