package es.amplia.oda.ruleengine.nashorn;

import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.State;
import es.amplia.oda.ruleengine.nashorn.configuration.RuleEngineConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.powermock.reflect.Whitebox;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NashornScriptTranslatorTest {

	NashornScriptTranslator testTranslator;

	@Mock
	ScriptEngine mockedEngine;
	@Mock
	State mockedState;
	@Mock
	Invocable mockedInvocable;

	@BeforeEach
	public void setUp() {
		testTranslator = new NashornScriptTranslator();
	}

	@Test
	public void testInitScript() throws Exception {
		String root = new File(".").getCanonicalPath();
		Whitebox.setInternalState(testTranslator, "jsUtilsPath", root + "/src/test/");
		File utilsFileToCreate = new File(root + "/src/test/utils.js");
		utilsFileToCreate.createNewFile();
		File ruleFilToCreate = new File(root + "/src/test/rule.js");
		ruleFilToCreate.createNewFile();
		FileWriter fileWriter = new FileWriter(ruleFilToCreate);
		fileWriter.write("function nothing(a, b) {return a}");
		fileWriter.close();

		String script = root + "/src/test/rule.js";
		testTranslator.initScript(script);

		assertTrue(((HashMap) Whitebox.getInternalState(testTranslator, "engines")).size() > 0);
		ruleFilToCreate.delete();
		utilsFileToCreate.delete();
	}

	@Test
	public void testInitScriptFileNotExists() throws Exception {
		String root = new File(".").getCanonicalPath();
		Whitebox.setInternalState(testTranslator, "jsUtilsPath", root + "/src/test/");
		File utilsFileToCreate = new File(root + "/src/test/utils.js");
		utilsFileToCreate.createNewFile();

		String script = "none file to do the test";
		testTranslator.initScript(script);

		assertTrue(((HashMap) Whitebox.getInternalState(testTranslator, "engines")).size() > 0);
		utilsFileToCreate.delete();
	}

	@Test
	public void testClose() {
		testTranslator.close();

		assertEquals(0, ((HashMap) Whitebox.getInternalState(testTranslator, "engines")).size());
	}

	@Test
	public void testPutAttribute() throws Exception {
		HashMap<String, ScriptEngine> map = new HashMap<>();
		map.put("rule.js", mockedEngine);
		Whitebox.setInternalState(testTranslator, "engines", map);

		testTranslator.putAttribute("rule.js", "att", true);

		assertTrue(((HashMap) Whitebox.getInternalState(testTranslator, "engines")).size() > 0);
	}

	@Test
	public void testRunMethod() throws ScriptException {
		HashMap<String, ScriptEngine> map = new HashMap<>();
		ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
		engine.eval("function nothing(a, b) {return a}");
		map.put("rule.js", engine);
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);
		Whitebox.setInternalState(testTranslator, "engines", map);

		State state = (State) testTranslator.runMethod("rule.js", "nothing", mockedState, value, null);

		assertEquals(mockedState, state);
	}

	@Test
	public void testRunMethodScriptException() throws ScriptException {
		HashMap<String, ScriptEngine> map = new HashMap<>();
		ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
		engine.eval("function nothing(a, b) {throw new ScriptException(\"Jej\")}");
		map.put("rule.js", engine);
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);
		Whitebox.setInternalState(testTranslator, "engines", map);

		State state = (State) testTranslator.runMethod("rule.js", "nothing", mockedState, value, null);

		assertEquals(mockedState, state);
	}

	@Test
	public void testRunMethodNoMethodException() throws ScriptException {
		HashMap<String, ScriptEngine> map = new HashMap<>();
		ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine();
		engine.eval("");
		map.put("rule.js", engine);
		DatastreamValue value = new DatastreamValue("testDevice", "testDatastream", "testFeed",
				System.currentTimeMillis(), true, DatastreamValue.Status.OK, "", false, false);
		Whitebox.setInternalState(testTranslator, "engines", map);

		State state = (State) testTranslator.runMethod("rule.js", "nothing", mockedState, value, null);

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
