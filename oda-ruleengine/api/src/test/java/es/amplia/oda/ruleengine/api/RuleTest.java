package es.amplia.oda.ruleengine.api;

import es.amplia.oda.core.commons.utils.DatastreamValue;
import es.amplia.oda.core.commons.utils.OsgiContext;
import es.amplia.oda.core.commons.utils.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.script.ScriptException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RuleTest {

	Rule testRule;
	@Mock
	ScriptTranslator mockedTranslator;
	@Mock
	State mockedState;
	@Mock
	OsgiContext mockedContext;

	@Test
	public void testConstructor() throws ScriptException {
		testRule = new Rule("Norma", Collections.singletonList("Presión"), mockedTranslator);

		verify(mockedTranslator).initScript("Norma");
	}

	@Test
	public void testConstructorException() throws ScriptException {
		doThrow(new ScriptException("")).when(mockedTranslator).initScript(anyString());

		assertThrows(ScriptException.class, () -> new Rule("Norma", Collections.singletonList("Presión"), mockedTranslator));
	}
	
	@Test
	public void testWhen() throws ScriptException {
		when(mockedTranslator.runMethod(anyString(), anyString(), any(), any(), any())).thenReturn(true);
		testRule = new Rule("Norma", Collections.singletonList("Presión"), mockedTranslator);
		DatastreamValue testDatastreamValue = new DatastreamValue("testDevice", "testDatastream",
				"feed", System.currentTimeMillis(), true, DatastreamValue.Status.OK, "",
				false, false);

		boolean result = testRule.when(mockedState, testDatastreamValue, mockedContext);

		assertTrue(result);
	}

	@Test
	public void testThen() throws ScriptException {
		when(mockedTranslator.runMethod(anyString(), anyString(), any(), any(), any())).thenReturn(mockedState);
		testRule = new Rule("Norma", Collections.singletonList("Presión"), mockedTranslator);
		DatastreamValue testDatastreamValue = new DatastreamValue("testDevice", "testDatastream",
				"feed", System.currentTimeMillis(), true, DatastreamValue.Status.OK, "",
				false, false);

		State result = testRule.then(mockedState, testDatastreamValue, mockedContext);

		assertEquals(mockedState, result);
	}
}
